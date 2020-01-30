package ru.avem.kspemstator.communication.devices.pr200

import com.ucicke.k2mod.modbus.ModbusException
import com.ucicke.k2mod.modbus.procimg.SimpleRegister
import org.slf4j.LoggerFactory
import ru.avem.kspemstator.communication.*
import ru.avem.kspemstator.communication.devices.Device
import ru.avem.kspemstator.communication.devices.Parameter
import ru.avem.kspemstator.communication.devices.enums.DeviceType
import ru.avem.kspemstator.communication.devices.enums.UnitID
import ru.avem.kspemstator.communication.devices.parameters.DeviceParameter
import ru.avem.kspemstator.utils.toInt
import java.util.*
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.math.pow


class OwenPR200Controller(private val unitID: UnitID, observer: Observer) : Observable(), Device {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)

        const val KMS_REG = 516
        const val SPEED_UP = 513
        const val SPEED_DOWN = 514
        const val PROTECTIONS_STATES_REG = 512
        const val WATCHDOG_REG = 520

        const val POWER: Short = 1
        const val DO2: Short = 2
        const val DO3: Short = 3
        const val DO4: Short = 4
        const val DO5: Short = 5
        const val UP: Short = 6
        const val DOWN: Short = 7

        val DEVICE_ID = DeviceType.PR200
    }

    enum class Parameters : Parameter {
        IS_RESPONDING,
        DI1, DI2, DI3, DI4, DI5, DI6, DI7, DI8
    }

    private var kms: Short = 0

    override var isResponding = false
        set(value) {
            field = value
            notice(DeviceParameter(unitID.id, DEVICE_ID, Parameters.IS_RESPONDING, field.toInt()))
        }

    init {
        addObserver(observer)
    }

    fun resetDog() {
        val dogReseter: Short = 1

        try {
            ModbusConnection.writeSingleRegister(
                unitID.id,
                WATCHDOG_REG,
                SimpleRegister(dogReseter)
            )
        } catch (e: Exception) {
            isResponding = false
        }
    }

    fun setUPSpeed(upSpeed: Int) {
        val speed: Int = upSpeed
        try {
            ModbusConnection.writeSingleRegister(
                unitID.id,
                SPEED_UP,
                SimpleRegister(speed)
            )
        } catch (e: Exception) {
            isResponding = false
        }
    }

    fun setDownSpeed(downSpeed: Int) {
        val speed: Int = downSpeed
        try {
            ModbusConnection.writeSingleRegister(
                unitID.id,
                SPEED_DOWN,
                SimpleRegister(speed)
            )
        } catch (e: Exception) {
            isResponding = false
        }
    }

    fun offAllKms() {
        kms = 0
        try {
            ModbusConnection.writeSingleRegister(unitID.id, KMS_REG, SimpleRegister(kms))
        } catch (e: Exception) {
            isResponding = false
        }
    }

    fun onRegisterInKMS(numberOfRegister: Short) {
        val nor = numberOfRegister - 1
        val mask = 2.0.pow(nor).toShort()
        kms = kms or mask

        try {
            ModbusConnection.writeSingleRegister(unitID.id, KMS_REG, SimpleRegister(kms))
        } catch (e: Exception) {
            isResponding = false
        }
    }

    fun offRegisterInKMS(numberOfRegister: Short) {
        val nor = numberOfRegister - 1
        val mask = 2.0.pow(nor).toInt().inv()
        kms = kms and mask.toShort()

        try {
            ModbusConnection.writeSingleRegister(unitID.id, KMS_REG, SimpleRegister(kms))
        } catch (e: Exception) {
            isResponding = false
        }
    }

    fun readProtectionsStatus() {
        try {
            val readInputRegisters = ModbusConnection.readInputRegisters(unitID.id, PROTECTIONS_STATES_REG, 1)
            val protectionsStatus = readInputRegisters[0].value
            logger.debug("Protections status: {}", protectionsStatus.toString())
            notifyProtectionsStatus(protectionsStatus)
        } catch (e: ModbusException) {
            isResponding = false
        }
    }

    private fun notifyProtectionsStatus(states: Int) {
        notice(DeviceParameter(unitID.id, DEVICE_ID, Parameters.DI1, states and END_MIN))
        notice(DeviceParameter(unitID.id, DEVICE_ID, Parameters.DI2, states and END_MAX))
        notice(DeviceParameter(unitID.id, DEVICE_ID, Parameters.DI3, states and IZM_COIL))
        notice(DeviceParameter(unitID.id, DEVICE_ID, Parameters.DI4, states and MAGNIT_COIL))
        notice(DeviceParameter(unitID.id, DEVICE_ID, Parameters.DI5, states and CURRENT_RELE))
        notice(DeviceParameter(unitID.id, DEVICE_ID, Parameters.DI6, states and IZM1))
        notice(DeviceParameter(unitID.id, DEVICE_ID, Parameters.DI7, states and IZM2))
        notice(DeviceParameter(unitID.id, DEVICE_ID, Parameters.DI8, states and IZM3))
    }

    private fun notice(parameter: DeviceParameter) {
        setChanged()
        notifyObservers(parameter)
    }
}
