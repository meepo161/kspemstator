package ru.avem.kspemstator.communication.devices.parmaT400

import com.ucicke.k2mod.modbus.ModbusIOException
import ru.avem.kspemstator.communication.ModbusConnection
import ru.avem.kspemstator.communication.devices.Device
import ru.avem.kspemstator.communication.devices.Parameter
import ru.avem.kspemstator.communication.devices.enums.DeviceType
import ru.avem.kspemstator.communication.devices.enums.UnitID
import ru.avem.kspemstator.communication.devices.parameters.DeviceParameter
import ru.avem.kspemstator.utils.toInt
import java.util.*

class ParmaT400Controller(private val unitID: UnitID, observer: Observer) : Observable(), Device {

    companion object {
        private const val UNIT_ID = 2

        private const val U_REGISTER: Int = 4
        private const val I_REGISTER: Int = 7

        var uAValue: Double = 0.0
        var uBValue: Double = 0.0
        var uCValue: Double = 0.0
        var iAValue: Double = 0.0
        var iBValue: Double = 0.0
        var iCValue: Double = 0.0
        var pAvalue: Double = 0.0

        val DEVICE_ID = DeviceType.PARMA
    }

    enum class Parameters : Parameter {
        IS_RESPONDING, U1, U2, U3, I1, I2, I3, P1
    }

    override var isResponding = false
        set(value) {
            field = value
            notice(
                DeviceParameter(
                    unitID.id,
                    DEVICE_ID,
                    Parameters.IS_RESPONDING,
                    field.toInt()
                )
            )
        }

    init {
        addObserver(observer)
    }

    fun readAllRegisters() {
        try {
            val registersValues = ModbusConnection.readInputRegisters(UNIT_ID, I_REGISTER, 9)
            iAValue = registersValues[0].value / 5000.0 * 2
            notice(DeviceParameter(unitID.id, DeviceType.PARMA, Parameters.I1, iAValue))
            iBValue = registersValues[1].value / 5000.0
            notice(DeviceParameter(unitID.id, DeviceType.PARMA, Parameters.I2, iBValue))
            iCValue = registersValues[2].value / 5000.0
            notice(DeviceParameter(unitID.id, DeviceType.PARMA, Parameters.I3, iCValue))

            uAValue = registersValues[4].value / 100.0
            notice(DeviceParameter(unitID.id, DeviceType.PARMA, Parameters.U1, uAValue))
            uBValue = registersValues[5].value / 100.0
            notice(DeviceParameter(unitID.id, DeviceType.PARMA, Parameters.U2, uBValue))
            uCValue = registersValues[6].value / 100.0
            notice(DeviceParameter(unitID.id, DeviceType.PARMA, Parameters.U3, uCValue))

            pAvalue = registersValues[8].value / 10.0 * 2
            notice(DeviceParameter(unitID.id, DeviceType.PARMA, Parameters.P1, pAvalue))
            this.isResponding = true
        } catch (m: ModbusIOException) {
            this.isResponding = false
        }
    }

    private fun notice(parameter: DeviceParameter) {
        setChanged()
        notifyObservers(parameter)
    }
}