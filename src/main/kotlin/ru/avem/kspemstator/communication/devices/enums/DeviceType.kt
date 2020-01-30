package ru.avem.kspemstator.communication.devices.enums

import ru.avem.kspemstator.communication.devices.Parameter
import ru.avem.kspemstator.communication.devices.parmaT400.ParmaT400Controller
import ru.avem.kspemstator.communication.devices.pr200.OwenPR200Controller

enum class DeviceType(val parameter: Class<out Parameter>) {
    PARMA(ParmaT400Controller.Parameters::class.java),
    PR200(OwenPR200Controller.Parameters::class.java)
}
