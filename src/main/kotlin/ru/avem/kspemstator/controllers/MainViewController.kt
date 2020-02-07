package ru.avem.kspemstator.controllers

import javafx.application.Platform
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import ru.avem.kspemstator.communication.CommunicationModel
import ru.avem.kspemstator.communication.devices.pr200.OwenPR200Controller.Companion.DO2
import ru.avem.kspemstator.communication.devices.pr200.OwenPR200Controller.Companion.DO3
import ru.avem.kspemstator.communication.devices.pr200.OwenPR200Controller.Companion.DO5
import ru.avem.kspemstator.communication.devices.pr200.OwenPR200Controller.Companion.DOWN
import ru.avem.kspemstator.communication.devices.pr200.OwenPR200Controller.Companion.POWER
import ru.avem.kspemstator.communication.devices.pr200.OwenPR200Controller.Companion.UP
import ru.avem.kspemstator.database.entities.*
import ru.avem.kspemstator.utils.Toast
import ru.avem.kspemstator.view.MainView
import tornadofx.Controller
import tornadofx.observable
import tornadofx.selectedItem
import java.text.SimpleDateFormat
import kotlin.math.abs

class MainViewController : Controller() {

    val view: MainView by inject()

    private var insideDiametr: Double = 0.0
    private var outsideDiametr: Double = 0.0
    private var l: Double = 0.0
    private var lossesMark: Double = 0.0
    private var intensityMark: Double = 0.0
    private var densityMark: Double = 0.0
    private var h: Double = 0.0
    private var lakt: Double = 0.0
    private var lsr: Double = 0.0
    private var sh: Double = 0.0
    private var v: Double = 0.0
    private var m: Double = 0.0
    private var uZadannoe: Double = 0.0
    private var ws: Int = 0
    private var measuringBf: Double = 0.0
    private var measuringPf: Double = 0.0
    private var measuringPt: Double = 0.0
    private var measuringHf: Double = 0.0

    @Volatile
    var measuringUA: Double = 0.0
    @Volatile
    var measuringUB: Double = 0.0
    @Volatile
    var measuringIA: Double = 0.0
    @Volatile
    var measuringPA: Double = 0.0
    @Volatile
    var measuringCos: Double = 0.0

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
                        setCause("Обесточьте установку\nПроверьте соединение измерительной катушки")
                    }
                    !CommunicationModel.di4 -> {
                        setCause(
                            "Обесточьте установку\n" +
                                    "Проверьте соединение намагничивающей катушки"
                        )
                    }
//                    CommunicationModel.di5 -> {
//                        setCause("di5")
//                    }
                    CommunicationModel.di6 && !CommunicationModel.di7 && CommunicationModel.di8 -> {
                        appendOneMessageToLog("Проверьте заземление")
                    }
                    CommunicationModel.di6 && !CommunicationModel.di7 && !CommunicationModel.di8 -> {
                        appendOneMessageToLog("Неверная фазировка питания стенда")
                    }
                    CommunicationModel.i1 > 15.0 -> {
                        setCause(
                            "Обесточьте установку\n" +
                                    "Превышение тока в установке"
                        )
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

    fun getPos1() {

    }

    fun calculate() {
        var p = 0.0
        transaction {
            val ourObjectType = MarksObjects.find() {
                MarksTypes.mark eq view.comboboxTypeObject.value.mark
            }.toList()
            p = ourObjectType[0].density.toDouble()
            lossesMark = ourObjectType[0].losses.toDouble()
            intensityMark = ourObjectType[0].intensity.toDouble()
            densityMark = ourObjectType[0].density.toDouble()
        }
        val ki: Double = if (view.comboboxTypeObject.value.insulation == "Лак") {
            0.93
        } else {
            0.95
        }
        insideDiametr = view.comboboxTypeObject.value.insideD.toDouble() //TODO может понадобится
        outsideDiametr = view.comboboxTypeObject.value.outsideD.toDouble()
        l = view.comboboxTypeObject.value.ironLength.toDouble()
        h = view.comboboxTypeObject.value.backHeight.toDouble()
        lakt = ki * l * 0.001
        lsr = Math.PI * (outsideDiametr - h) * 0.001
        sh = lakt * h * 0.001
        v = lsr * sh
        m = v * p
        uZadannoe = 4.44 * 22 * 50 * sh
    }

    fun stopExperiment() {
        setCause("Отменено оператором")
        isExperimentRunning = false
        view.buttonStartExperiment.isDisable = true
    }

    fun startExperiment() {
        getPos1()
        calculate()

        Platform.runLater {
            view.hboxEdit.isDisable = true
            view.mainMenubar.isDisable = true
            view.buttonStartExperiment.text = "Отменить"
            view.textAreaExperiment.text = ""
        }
        cause = ""
        isExperimentRunning = true

        Thread {

            if (uZadannoe > 150) {
                cause = "Напряжение больше допустимого"
                isExperimentRunning = false
            } else {
                appendOneMessageToLog("Напряжение расчетное = " + String.format("%.2f В", uZadannoe))
            }

            if (isExperimentRunning) {
                appendOneMessageToLog("Запуск испытания")
            }
            var tryNumberPR = 0
            while (isExperimentRunning && !CommunicationModel.owenPR200Controller.isResponding) {
                tryNumberPR++
                Thread.sleep(100)
                if (tryNumberPR > 100) {
                    setCause("Нет связи с ПР200")
                }
            }

            var tryNumberParma = 0
            while (isExperimentRunning && !CommunicationModel.parmaT400Controller.isResponding) {
                tryNumberParma++
                Thread.sleep(100)
                if (tryNumberParma > 100) {
                    setCause("Нет связи с ПармаТ400")
                }
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
                    uZadannoe <= 5 -> {
                        CommunicationModel.owenPR200Controller.onRegisterInKMS(DO3)
                        ws = 10
                    }
                    uZadannoe <= 40 -> {
                        CommunicationModel.owenPR200Controller.onRegisterInKMS(DO3)
                        CommunicationModel.owenPR200Controller.onRegisterInKMS(DO5)
                        ws = 21
                    }
                    uZadannoe <= 150 -> {
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
                controlVoltage()
                regulationVoltage(uZadannoe)
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
                saveProtocolToDB()
            }

            Platform.runLater {
                view.hboxEdit.isDisable = false
                view.mainMenubar.isDisable = false
                view.buttonStartExperiment.isDisable = false
                view.buttonStartExperiment.text = "Испытание"
            }
        }.start()
    }

    private fun controlVoltage() {
        Thread {
            val lastU = CommunicationModel.u2
            Thread.sleep(10000)
            if (lastU * 1.1 > CommunicationModel.u2) {
                setCause("Нет напряжения\nПроверьте соединение кабеля")
            }
        }.start()
    }

    private fun regulationVoltage(voltage: Double) {
        while (isExperimentRunning && isDevicesResponding && (CommunicationModel.u2 <= voltage * 0.96 || CommunicationModel.u2 > voltage * 1.04)) {
            if (CommunicationModel.u2 <= voltage * 0.96) {
                CommunicationModel.owenPR200Controller.offRegisterInKMS(DOWN)
                CommunicationModel.owenPR200Controller.onRegisterInKMS(UP)
            } else if (CommunicationModel.u2 > voltage * 1.04) {
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
        measuringBf = measuringUB / (4.44 * 22 * 50 * sh)
        measuringPf = measuringPA * (21.0 / 22.0) * (1.0 / (measuringBf * measuringBf))
        measuringPt = if (view.comboboxTypeObject.selectedItem!!.material == "Сталь") {
            measuringPf / m - ((measuringPf / m) / 10.0)
        } else {
            measuringPf / m
        }
        measuringHf = (measuringIA * 21) / lsr

        Platform.runLater {
            appendOneMessageToLog(String.format("Фактическая индукция Bf = %.4f Тл", measuringBf))
            appendOneMessageToLog(String.format("Фактическая активная мощность Pf = %.4f Вт", measuringPf))
            appendOneMessageToLog(String.format("Удельные потери Pt = %.4f Вт/кг", measuringPt))
            appendOneMessageToLog(String.format("Напряженность Hf =  %.4f А/м", measuringHf))
            appendOneMessageToLog("Заключение:")

            if (lossesMark >= measuringPt) {
                appendOneMessageToLog(
                    String.format(
                        "Запас по удельным потерям %.2f",
                        abs((measuringPt / (lossesMark / 100)) - 100)
                    ) + "%"
                )
            } else {
                appendOneMessageToLog(
                    String.format(
                        "Превышение по удельным потерям %.2f",
                        abs((measuringPt / (lossesMark / 100)) - 100)
                    ) + "%"
                )
            }

            if (intensityMark >= measuringHf) {
                appendOneMessageToLog(
                    String.format(
                        "Запас по магнитным свойствам %.2f",
                        abs((measuringHf / (intensityMark / 100)) - 100)
                    ) + "%"
                )
            } else {
                appendOneMessageToLog(
                    String.format(
                        "Превышение по магнитным свойствам %.2f",
                        abs((measuringHf / (intensityMark / 100)) - 100)
                    ) + "%"
                )
            }
        }
    }

    private fun saveProtocolToDB() {
        val dateFormatter = SimpleDateFormat("dd.MM.y")
        val timeFormatter = SimpleDateFormat("HH:mm:ss")

        val unixTime = System.currentTimeMillis()

        transaction {
            Protocol.new {
                date = dateFormatter.format(unixTime).toString()
                time = timeFormatter.format(unixTime).toString()
                factoryNumber = view.textFieldFacNumber.text
                objectType = view.comboboxTypeObject.selectedItem.toString()
                power = view.comboboxTypeObject.value.power
                frequency = view.comboboxTypeObject.value.frequency
                mark = view.comboboxTypeObject.value.mark
                density = densityMark.toString()
                losses = lossesMark.toString()
                intensity = intensityMark.toString()
                pos1 = "Иванов И.И."
                u1 = String.format("%.2f", measuringUA)
                i1 = String.format("%.2f", measuringIA)
                p1 = String.format("%.2f", measuringPA)
                bf = String.format("%.2f", measuringBf)
                pf = String.format("%.2f", measuringPf)
                pt = String.format("%.2f", measuringPt)
                hf = String.format("%.2f", measuringHf)
            }
        }
        appendMessageToLog("Протокол сохранён")
    }
}
