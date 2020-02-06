package ru.avem.kspemstator.communication

import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory
import ru.avem.kspemstator.communication.ModbusConnection.isAppRunning
import ru.avem.kspemstator.communication.ModbusConnection.isModbusConnected
import ru.avem.kspemstator.communication.devices.Device
import ru.avem.kspemstator.communication.devices.enums.DeviceType
import ru.avem.kspemstator.communication.devices.enums.UnitID
import ru.avem.kspemstator.communication.devices.parameters.DeviceParameter
import ru.avem.kspemstator.communication.devices.parmaT400.ParmaT400Controller
import ru.avem.kspemstator.communication.devices.pr200.OwenPR200Controller
import java.lang.Thread.sleep
import java.util.*

object CommunicationModel : Observer {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val marker = MarkerFactory.getMarker("DEVICE")

    val owenPR200Controller = OwenPR200Controller(UnitID.PR200, this)
    val parmaT400Controller = ParmaT400Controller(UnitID.PARMA, this)

    val deviceControllers =
        listOf<Device>(
            owenPR200Controller, parmaT400Controller
        )

    var di1 = false
    var di2 = false
    var di3 = false
    var di4 = false
    var di5 = false
    var di6 = false
    var di7 = false
    var di8 = false
    var u1 = 0.0
    var u2 = 0.0
    var u3 = 0.0
    var i1 = 0.0
    var i2 = 0.0
    var i3 = 0.0
    var p1 = 0.0
    var cosA = 0.0

    init {
        Thread {
            while (isAppRunning) {
                deviceControllers.forEach {
                    if (isModbusConnected) {
                        try {
                            when (it) {
                                is OwenPR200Controller -> {
                                    try {
                                        it.resetDog()
                                        it.readProtectionsStatus()
                                        it.isResponding = true
                                    } catch (e: Exception) {
                                        it.isResponding = false
                                    }
                                }
                                is ParmaT400Controller -> {
                                    try {
                                        it.readAllRegisters()
                                        it.readCos()
                                        it.isResponding = true
                                    } catch (e: Exception) {
                                        it.isResponding = false
                                    }
                                }
                            }
                        } catch (e: NullPointerException) {
                        }
                    }
                }
                sleep(1)
            }
        }.start()
    }

    fun checkDevices(): List<String> {
        val causes = ArrayList<String>()
        deviceControllers.forEach {
            when (it) {
                is OwenPR200Controller -> {
                    if (!it.isResponding) {
                        causes.add("ПР200")
                    }
                }
                is ParmaT400Controller -> {
                    if (!it.isResponding) {
                        causes.add("ПармаТ400")
                    }
                }
            }
        }

        return causes
    }

    override fun update(o: Observable?, arg: Any?) {
        arg as DeviceParameter
        val value = arg.value
        when (arg.device) {
            DeviceType.PR200 -> {
                when (arg.parameter) {
                    OwenPR200Controller.Parameters.DI1 -> di1 = value == 1.0
                    OwenPR200Controller.Parameters.DI2 -> di2 = value == 2.0
                    OwenPR200Controller.Parameters.DI3 -> di3 = value == 4.0
                    OwenPR200Controller.Parameters.DI4 -> di4 = value == 8.0
                    OwenPR200Controller.Parameters.DI5 -> di5 = value == 16.0
                    OwenPR200Controller.Parameters.DI6 -> di6 = value == 32.0
                    OwenPR200Controller.Parameters.DI7 -> di7 = value == 64.0
                    OwenPR200Controller.Parameters.DI8 -> di8 = value == 128.0
                }
            }
            DeviceType.PARMA -> {
                when (arg.parameter) {
                    ParmaT400Controller.Parameters.U1 -> u1 = value
                    ParmaT400Controller.Parameters.U2 -> u2 = value
                    ParmaT400Controller.Parameters.U3 -> u3 = value
                    ParmaT400Controller.Parameters.I1 -> i1 = value
                    ParmaT400Controller.Parameters.I2 -> i2 = value
                    ParmaT400Controller.Parameters.I3 -> i3 = value
                    ParmaT400Controller.Parameters.P1 -> p1 = value
                    ParmaT400Controller.Parameters.COSA -> cosA = value
                }
            }
        }
    }

//        logger.info(
//            marker,
//            "Values: km2: {} | amperageProtectionBeforeLatr: {} | amperageProtectionAfterLatr1: {} | amperageProtectionAfterLatr2: {} | doorProtection: {}",
//            km2,
//            amperageProtectionBeforeLatr,
//            amperageProtectionAfterLatr1,
//            amperageProtectionAfterLatr2,
//            doorProtection
//        )
//
//        logger.info(
//            marker,
//            "Values: latrStatus: {} | latrVoltage: {} | latrHiEnd: {} | latrLowEnd: {}",
//            latrControllerStatus,
//            latrControllerVoltage,
//            latrControllerHiEnd,
//            latrControllerLowEnd
//        )
//        logger.info(
//            marker, "Values: voltage: {}",
//            voltage
//        )
//        logger.info(
//            marker, "Values: amperage: {}",
//            amperage
//        )
}
