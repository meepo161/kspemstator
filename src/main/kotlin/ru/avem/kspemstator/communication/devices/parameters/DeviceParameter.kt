package ru.avem.kspemstator.communication.devices.parameters

import ru.avem.kspemstator.communication.devices.Parameter
import ru.avem.kspemstator.communication.devices.enums.DeviceType

class DeviceParameter(val unitID: Int, val device: DeviceType, val parameter: Parameter, value: Number) {
    val value = value.toDouble()

    init {
        if (device.parameter != parameter.javaClass) {
            throw IllegalArgumentException("${device.parameter} != ${parameter.javaClass}")
        }
    }
}
