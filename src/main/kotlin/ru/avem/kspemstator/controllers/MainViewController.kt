package ru.avem.kspemstator.controllers

import javafx.application.Platform
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import ru.avem.kspemstator.communication.CommunicationModel
import ru.avem.kspemstator.communication.devices.pr200.OwenPR200Controller.Companion.DO2
import ru.avem.kspemstator.communication.devices.pr200.OwenPR200Controller.Companion.DO3
import ru.avem.kspemstator.communication.devices.pr200.OwenPR200Controller.Companion.DO4
import ru.avem.kspemstator.communication.devices.pr200.OwenPR200Controller.Companion.DO5
import ru.avem.kspemstator.communication.devices.pr200.OwenPR200Controller.Companion.DOWN
import ru.avem.kspemstator.communication.devices.pr200.OwenPR200Controller.Companion.POWER
import ru.avem.kspemstator.communication.devices.pr200.OwenPR200Controller.Companion.UP
import ru.avem.kspemstator.database.entities.ExperimentObjectsType
import ru.avem.kspemstator.database.entities.MarksObjects
import ru.avem.kspemstator.database.entities.MarksTypes
import ru.avem.kspemstator.database.entities.ObjectsTypes
import ru.avem.kspemstator.utils.Toast
import ru.avem.kspemstator.view.MainView
import tornadofx.Controller
import tornadofx.observable
import java.text.SimpleDateFormat

class MainViewController : Controller() {

    val view: MainView by inject()

    private var insideD: Double = 0.0
    private var outsideD: Double = 0.0
    private var l: Double = 0.0
    private var losses: Double = 0.0
    private var intensity: Double = 0.0
    private var h: Double = 0.0
    private var lakt: Double = 0.0
    private var lsr: Double = 0.0
    private var sh: Double = 0.0
    private var v: Double = 0.0
    private var m: Double = 0.0
    private var u: Double = 0.0
    private var ws: Int = 0
    private var bf: Double = 0.0
    private var pf: Double = 0.0
    private var pt: Double = 0.0
    private var hf: Double = 0.0

    @Volatile
    var measuringUA: Double = 0.0
    @Volatile
    var measuringUB: Double = 0.0
    @Volatile
    var measuringIA: Double = 0.0
    @Volatile
    var measuringPA: Double = 0.0

    @Volatile
    var isExperimentRunning = false
    @Volatile
    private var cause: String? = null

    private val sdf = SimpleDateFormat("HH:mm:ss-SSS")
    private var logBuffer: String? = null

    private val isDevicesResponding: Boolean
        get() = CommunicationModel.owenPR200Controller.isResponding &&
                CommunicationModel.parmaT400Controller.isResponding

    private fun setCause(cause: String) {
        this.cause = cause
        if (cause.isNotEmpty()) {
            isExperimentRunning = false
        }
    }

    private fun checkProtections() {
        Thread {
            while (isExperimentRunning) {
                when {
                    !CommunicationModel.di3 -> {
                        setCause("di3")
                    }
                    !CommunicationModel.di4 -> {
                        setCause("di4")
                    }
//                    CommunicationModel.di5 -> {
//                        setCause("di5")
//                    }
                    CommunicationModel.di6 -> {
//                        appendOneMessageToLog("Проверьте заземление. Переверните сетевую вилку")
                    }
                    CommunicationModel.di7 -> {
//                        appendOneMessageToLog("Проверьте заземление. Переверните сетевую вилку")
                    }
                    !CommunicationModel.di8 -> {
//                        appendOneMessageToLog("Проверьте заземление. Переверните сетевую вилку")
                    }
                    CommunicationModel.i1 > 15 -> {
                        setCause("Ток превысил 15")
                    }
                }
            }
        }.start()
    }

    fun showAboutUs() {
        Toast.makeText("Версия ПО: 0.0.1b, Дата: 05.12.2019").show(Toast.ToastType.INFORMATION)
    }

    fun refreshObjectsTypes() {
        val selectedIndex = view.comboboxTypeObject.selectionModel.selectedIndex
        view.comboboxTypeObject.items = transaction {
            ExperimentObjectsType.all().toList().observable()
        }
        view.comboboxTypeObject.selectionModel.select(selectedIndex)
    }

    fun deleteTestItem() {
        transaction {
            ObjectsTypes.deleteWhere {
                ObjectsTypes.objectType eq view.comboboxTypeObject.selectionModel.selectedItem.objectType
            }
        }
        refreshObjectsTypes()
    }


    private fun appendOneMessageToLog(message: String) {
        if (logBuffer == null || logBuffer != message) {
            logBuffer = message
            appendMessageToLog(message)
        }
    }

    private fun appendMessageToLog(message: String) {
        Platform.runLater {
            view.textAreaExperiment.appendText(
                String.format(
                    "%s \t| %s\n",
                    sdf.format(System.currentTimeMillis()),
                    message
                )
            )
        }
    }

    fun calculate() {
        when {
            view.comboboxTypeObject.selectionModel.selectedItem == null -> {
                Toast.makeText("Выберите тип двигателя").show(Toast.ToastType.WARNING)
            }
            view.textFieldFacNumber.text.isNullOrEmpty() -> {
                Toast.makeText("Введите заводской номер двигателя").show(Toast.ToastType.WARNING)
            }
            else -> {
                var p = 0.0
                transaction {
                    val ourObjectType = MarksObjects.find() {
                        MarksTypes.mark eq view.comboboxTypeObject.value.mark
                    }.toList()
                    p = ourObjectType[0].density.toDouble()
                    losses = ourObjectType[0].losses.toDouble()
                    intensity = ourObjectType[0].intensity.toDouble()
                }
                val ki: Double = if (view.comboboxTypeObject.value.insulation == "Лак") {
                    0.93
                } else {
                    0.95
                }
                insideD = view.comboboxTypeObject.value.insideD.toDouble() //TODO может понадобится
                outsideD = view.comboboxTypeObject.value.outsideD.toDouble()
                l = view.comboboxTypeObject.value.ironLength.toDouble()
                h = view.comboboxTypeObject.value.backHeight.toDouble()
                lakt = ki * l * 0.001
                lsr = Math.PI * (outsideD - h) * 0.001
                sh = lakt * h * 0.001
                v = lsr * sh
                m = v * p
                u = 4.44 * 22 * 50 * sh
            }
        }
    }

    fun stopExperiment() {
        setCause("Отменено оператором")
        isExperimentRunning = false
        view.buttonStartExperiment.isDisable = true
    }

    fun startExperiment() {
        calculate()

        Platform.runLater {
            view.hboxEdit.isDisable = true
            view.buttonStartExperiment.text = "Отменить"
            view.textAreaExperiment.text = ""
        }
        cause = ""
        isExperimentRunning = true

        Thread {

            if (u > 150) {
                cause = "Напряжение больше допустимого"
                isExperimentRunning = false
            } else {
                appendOneMessageToLog("Напряжение расчитанное = " + String.format("%.2f", u))
            }

            if (isExperimentRunning) {
                appendOneMessageToLog("Запуск испытания")
            }

            while (isExperimentRunning && !CommunicationModel.owenPR200Controller.isResponding) {
                Thread.sleep(100)
            }

            while (isExperimentRunning && !CommunicationModel.parmaT400Controller.isResponding) {
                Thread.sleep(100)
            }

            if (isExperimentRunning && isDevicesResponding) {
                checkProtections()
            }

            if (isExperimentRunning && !CommunicationModel.di1 && isDevicesResponding) {
                CommunicationModel.owenPR200Controller.onRegisterInKMS(DOWN)
            }

            while (isExperimentRunning && !CommunicationModel.di1 && isDevicesResponding) {
                Thread.sleep(1)
            }

            if (isExperimentRunning && isDevicesResponding) {
                CommunicationModel.owenPR200Controller.offRegisterInKMS(DOWN)
            }

            if (isExperimentRunning && isDevicesResponding) {
                CommunicationModel.owenPR200Controller.onRegisterInKMS(POWER)
                when {
                    u <= 5 -> {
                        CommunicationModel.owenPR200Controller.onRegisterInKMS(DO4)
                        CommunicationModel.owenPR200Controller.onRegisterInKMS(DO3)
                        ws = 10
                    }
                    u <= 40 -> {
                        CommunicationModel.owenPR200Controller.onRegisterInKMS(DO3)
                        CommunicationModel.owenPR200Controller.onRegisterInKMS(DO5)
                        ws = 21
                    }
                    u <= 150 -> {
                        CommunicationModel.owenPR200Controller.onRegisterInKMS(DO2)
                        CommunicationModel.owenPR200Controller.onRegisterInKMS(DO5)
                        ws = 21
                    }
                }
            }

            if (isExperimentRunning && isDevicesResponding) {
                Thread.sleep(200)
                CommunicationModel.owenPR200Controller.onRegisterInKMS(UP)
            }

            if (isExperimentRunning) {
                controlVoltage()
            }

            if (isExperimentRunning && isDevicesResponding) {
                regulationVoltage(u)
                showResults()
            }

            CommunicationModel.owenPR200Controller.offRegisterInKMS(UP)

            CommunicationModel.owenPR200Controller.onRegisterInKMS(DOWN)
            while (!CommunicationModel.di1 && isDevicesResponding) {
                Thread.sleep(1)
            }

            CommunicationModel.owenPR200Controller.offRegisterInKMS(DOWN)

            CommunicationModel.owenPR200Controller.offAllKms()
            isExperimentRunning = false

            if (cause != "") {
                appendOneMessageToLog(String.format("Прервано по причине: %s", cause))
            } else if (!isDevicesResponding) {
                appendOneMessageToLog("Потеряна связь с приборами")
            } else {
                appendOneMessageToLog("Испытание завершено успешно")
            }

            Platform.runLater {
                view.hboxEdit.isDisable = false
                view.buttonStartExperiment.isDisable = false
                view.buttonStartExperiment.text = "Испытание"
            }
        }.start()
    }

    private fun controlVoltage() {
        Thread {
            val lastU = CommunicationModel.u2
            Thread.sleep(5000)
            if (lastU * 1.1 > CommunicationModel.u2) {
                setCause("Нет напряжения\nПроверьте соединение кабеля")
            }
        }.start()
    }

    private fun regulationVoltage(voltage: Double) {
        while (isExperimentRunning && isDevicesResponding && (CommunicationModel.u2 <= voltage * 0.99 || CommunicationModel.u2 > voltage * 1.01)) {
            if (CommunicationModel.u2 <= voltage * 0.99) {
                CommunicationModel.owenPR200Controller.offRegisterInKMS(DOWN)
                CommunicationModel.owenPR200Controller.onRegisterInKMS(UP)
            } else if (CommunicationModel.u2 > voltage * 1.01) {
                CommunicationModel.owenPR200Controller.offRegisterInKMS(UP)
                CommunicationModel.owenPR200Controller.onRegisterInKMS(DOWN)
            }
            if (isExperimentRunning && CommunicationModel.di2 && isDevicesResponding) {
                setCause("Достигли концевика MAX")
                break
            }
        }

        measuringUA = CommunicationModel.u1
        measuringUB = CommunicationModel.u2
        measuringIA = CommunicationModel.i1
        measuringPA = CommunicationModel.p1
        appendOneMessageToLog(String.format("Напряжение = %.2f В", measuringUB))
        appendOneMessageToLog(String.format("Ток It =  %.2f А", measuringIA))
        appendOneMessageToLog(String.format("Напряжение Ut = %.2f В", measuringUA))
        appendOneMessageToLog(String.format("Активная мощность Pt = %.2f Вт", measuringPA))

        CommunicationModel.owenPR200Controller.offRegisterInKMS(UP)
        CommunicationModel.owenPR200Controller.offRegisterInKMS(DOWN)
    }

    private fun showResults() {
        bf = measuringUB / (4.44 * 22 * 50 * sh)
        pf = measuringPA * (21.0 / 22.0) * (1.0 / (bf * bf))
        pt = pf / m
        hf = (measuringIA * 21) / lsr

        Platform.runLater {
            appendOneMessageToLog(String.format("Фактическая индукция Bf = %.4f Тл", bf))
            appendOneMessageToLog(String.format("Фактическая активная мощность Pf = %.4f Вт", pf))
            appendOneMessageToLog(String.format("Удельные потери Pt = %.4f Вт/кг", pt))
            appendOneMessageToLog(String.format("Напряженность Hf =  %.4f А/м", hf))
            appendOneMessageToLog("Заключение:")
            if (losses >= pt) {
                appendOneMessageToLog(String.format("Запас по удельным потерям в %.2f раз(а)", losses/pt))
            } else {
                appendOneMessageToLog(String.format("Превышение по удельным потерям в %.2f раз(а)", losses/pt))
            }

            if (intensity > hf) {
                appendOneMessageToLog(String.format("Запас по магнитным свойствам в %.2f раз(а)", intensity/hf))
            } else {
                appendOneMessageToLog(String.format("Превышение по магнитным свойствам в %.2f раз(а)", intensity/hf))
            }
        }
    }

}
