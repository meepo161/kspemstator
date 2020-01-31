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
import tornadofx.selectedItem
import java.text.SimpleDateFormat

class MainViewController : Controller() {

    val view: MainView by inject()

    private var insideD: Double = 0.0
    private var outsideD: Double = 0.0
    private var l: Double = 0.0
    private var h: Double = 0.0
    private var lakt: Double = 0.0
    private var lsr: Double = 0.0
    private var sh: Double = 0.0
    private var v: Double = 0.0
    private var m: Double = 0.0
    private var u: Double = 0.0
    private var ws: Int = 0

    @Volatile
    var measuringUA: Double = 0.0
    @Volatile
    var measuringUB: Double = 0.0
    @Volatile
    var measuringUC: Double = 0.0
    @Volatile
    var measuringIA: Double = 0.0
    @Volatile
    var measuringIB: Double = 0.0
    @Volatile
    var measuringIC: Double = 0.0
    @Volatile
    var measuringPA: Double = 0.0
    @Volatile
    var measuringPB: Double = 0.0
    @Volatile
    var measuringPC: Double = 0.0

    @Volatile
    var isExperimentRunning = false
    @Volatile
    var isValuesNeed = false
    @Volatile
    var isNeedCheckEarth = false
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
                        appendOneMessageToLog("Проверьте заземление. Переверните сетевую вилку")
                    }
                    CommunicationModel.di7 -> {
                        appendOneMessageToLog("Проверьте заземление. Переверните сетевую вилку")
                    }
                    !CommunicationModel.di8 -> {
                        appendOneMessageToLog("Проверьте заземление. Переверните сетевую вилку")
                    }
                    CommunicationModel.i1 > 10 -> {
                        setCause("Ток превысил 10")
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
                    var ourObjectType = MarksObjects.find() {
                        MarksTypes.mark eq view.comboboxTypeObject.value.mark
                    }.toList()
                    p = ourObjectType[0].density.toDouble()
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
                Platform.runLater {
                    view.textAreaCalculate.text = "1.Длина активной стали Lакт = " + String.format("%.4f м.", lakt) +
                            "\n2.Длина средней линии железа Lср = " + String.format("%.4f м.", lsr) +
                            "\n3.Сечение спинки Sh = " + String.format("%.4f м².", sh) +
                            "\n4.Объем железа V = " + String.format("%.4f м³.", v) +
                            "\n5.Масса железа M = " + String.format("%.4f кг.", m) +
                            "\n6.Расчетное напряжение Uo = " + String.format("%.4f В.", u)

                    view.textAreaTotalInfo.text = "Испытатель №1: Иванов И.И." +
                            "\nИспытатель №2: Петров П.П." +
                            "\nТип двигателя: ${view.comboboxTypeObject.selectedItem.toString()}" +
                            "\n№ двигателя: ${view.textFieldFacNumber.text}"

                    view.textFieldU.text = String.format("%.2f", u)

                    Toast.makeText("Выполнен расчет").show(Toast.ToastType.INFORMATION)
                    view.buttonStartExperiment.isDisable = false
                }
            }
        }
    }

    private fun showResults() {
        val bf = measuringIB / (4.44 * 22 * 50 * sh)
        val pf = measuringPA * (21 / 22) * (1.0 / (bf * bf))
        val pt = pf / m
        val hf = (measuringIA * 21) / lsr

//        if (cause == "") {
        view.textAreaResults.text = "1.Фактическая индукция:" +
                "\nBf = " + String.format("%.4f bf = measuringIB / (4.44 * 22 * 50 * sh) ", bf) +
                "\n2.Фактическая активная мощность" +
                "\nPf = " + String.format("%.4f measuringPA * (21 / 22) * (1.0 / (bf * bf))", pf) +
                "\n3.Удельные потери" +
                "\nPt = " + String.format("%.4f  pf / m", pt) +
                "\n4.Напряженность" +
                "\nHf = " + String.format("%.4f  v = lsr * sh", hf)
//        }
    }

    fun stopExperiment() {
        setCause("Отменено оператором")
        isExperimentRunning = false
        view.buttonStartExperiment.isDisable = true
    }

    fun startExperiment() {
        Platform.runLater {
            view.hboxEdit.isDisable = true
            view.buttonCalculation.isDisable = true
            view.buttonStartExperiment.text = "Отменить"
            view.textAreaExperiment.text = ""
        }
        cause = ""
        isExperimentRunning = true

        Thread {

            if (u > 150) {
                cause = "Напряжение больше допустимого"
                isExperimentRunning = false
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

            if (isExperimentRunning && isDevicesResponding) {
                viewMeasuringValues()
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
                    u <= 50 -> {
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

            while (isExperimentRunning && CommunicationModel.u2 < u && isDevicesResponding) {
                Thread.sleep(1)
                if (isExperimentRunning && CommunicationModel.di2 && isDevicesResponding) {
                    setCause("Достигли концевика MAX")
                    break
                }
            }

            CommunicationModel.owenPR200Controller.offRegisterInKMS(UP)

            if (isExperimentRunning && isDevicesResponding) {
                appendOneMessageToLog("Началось измерение")
                Thread.sleep(1000)
                measuringUA = CommunicationModel.u1
                measuringUB = CommunicationModel.u2
                measuringUC = CommunicationModel.u3
                measuringIA = CommunicationModel.i1
                measuringIB = CommunicationModel.i2
                measuringIC = CommunicationModel.i3
                measuringPA = CommunicationModel.p1
                appendOneMessageToLog("Напряжение = " + String.format("%.2f", measuringUB))
                appendOneMessageToLog("Ток It = " + String.format("%.2f", measuringIA))
                appendOneMessageToLog("Напряжение Ut = " + String.format("%.2f", measuringUA))
                appendOneMessageToLog("Активная мощность Pt = " + String.format("%.2f", measuringPA))
            }
            isValuesNeed = false


            CommunicationModel.owenPR200Controller.onRegisterInKMS(DOWN)
            while (!CommunicationModel.di1 && isDevicesResponding) {
                Thread.sleep(1)
            }
            if (isExperimentRunning && isDevicesResponding) {
            }

            CommunicationModel.owenPR200Controller.offRegisterInKMS(DOWN)

            CommunicationModel.owenPR200Controller.offAllKms()
            isExperimentRunning = false

            if (cause != "") {
                appendOneMessageToLog(String.format("Испытание прервано по причине: \n%s", cause))
            } else if (!isDevicesResponding) {
                appendOneMessageToLog("Потеряна связь с приборами")
                setCause("Потеряна связь с приборами")
            } else {
                appendOneMessageToLog("Испытание завершено успешно")
            }

            showResults()

            Platform.runLater {
                view.hboxEdit.isDisable = false
                view.buttonCalculation.isDisable = false
                view.buttonStartExperiment.isDisable = false
                view.buttonStartExperiment.text = "Испытание"
            }
        }.start()
    }

    private fun controlVoltage() {
        Thread {
            var lastU = CommunicationModel.u2
            Thread.sleep(5000)
            if (lastU * 1.1 > CommunicationModel.u2) {
                setCause("Нет напряжения\nПроверьте соединение кабеля")
            }
        }.start()
    }

    private fun viewMeasuringValues() {
        Thread {
            isValuesNeed = true
            if (isExperimentRunning && isDevicesResponding) {
                while (isValuesNeed && isExperimentRunning && isDevicesResponding) {
                    Platform.runLater {
                        var measuringU2 = CommunicationModel.u2
                        var measuringU1 = CommunicationModel.u1
                        var measuringI1 = CommunicationModel.i1
                        var measuringP1 = CommunicationModel.p1

                        if (isValuesNeed && isExperimentRunning && isDevicesResponding) {
                            measuringU2 = CommunicationModel.u2
                            measuringU1 = CommunicationModel.u1
                            measuringI1 = CommunicationModel.i1
                            measuringP1 = CommunicationModel.p1
                        }
                        view.textFieldU2.text = String.format("%.2f", measuringU2)
                        view.textFieldU1.text = String.format("%.2f", measuringU1)
                        view.textFieldI1.text = String.format("%.2f", measuringI1)
                        view.textFieldP1.text = String.format("%.2f", measuringP1)
                    }
                    Thread.sleep(100)
                }
            }
        }.start()
    }
}
