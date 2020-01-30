package ru.avem.kspemstator.controllers

import javafx.application.Platform
import javafx.scene.control.Alert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import ru.avem.kspemstator.communication.CommunicationModel
import ru.avem.kspemstator.communication.devices.pr200.OwenPR200Controller.Companion.DO2
import ru.avem.kspemstator.communication.devices.pr200.OwenPR200Controller.Companion.DO3
import ru.avem.kspemstator.communication.devices.pr200.OwenPR200Controller.Companion.DO4
import ru.avem.kspemstator.communication.devices.pr200.OwenPR200Controller.Companion.DO5
import ru.avem.kspemstator.communication.devices.pr200.OwenPR200Controller.Companion.DOWN
import ru.avem.kspemstator.communication.devices.pr200.OwenPR200Controller.Companion.POWER
import ru.avem.kspemstator.communication.devices.pr200.OwenPR200Controller.Companion.UP
import ru.avem.kspemstator.database.entities.*
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
                    CommunicationModel.i1 > 10 -> {
                        setCause("Ток превысил 10")
                    }
                }
            }
        }.start()
    }

    fun isValuesEmpty(): Boolean {
        return view.comboboxTypeObject.selectionModel.isEmpty ||
                view.textFieldFacNumber.text.isNullOrEmpty() ||
                view.textFieldOutside.text.isNullOrEmpty() ||
                view.textFieldInside.text.isNullOrEmpty() ||
                view.textFieldBackHeight.text.isNullOrEmpty() ||
                view.textFieldIronLength.text.isNullOrEmpty() ||
                view.comboBoxMaterial.selectionModel.selectedItem.isNullOrEmpty() ||
                view.comboBoxInsulation.selectionModel.selectedItem.isNullOrEmpty()
    }

    fun isValuesDouble(): Boolean {
        return try {
            view.textFieldOutside.text.toDouble()
            view.textFieldInside.text.toDouble()
            view.textFieldBackHeight.text.toDouble()
            view.textFieldIronLength.text.toDouble()
            true
        } catch (e: Exception) {
            Toast.makeText("Неверно заполнены поля").show(Toast.ToastType.ERROR)
            false
        }
    }

    fun showAboutUs() {
        val alert = Alert(Alert.AlertType.INFORMATION)
        alert.title = "Версия ПО"
        alert.headerText = "Версия: 0.0.1b"
        alert.contentText = "Дата: 05.12.2019"
        alert.showAndWait()
    }

    fun refreshUsers() {
        view.comboboxUserSelector.items = transaction {
            User.find {
                Users.login notLike "admin"
            }.toList().observable()
        }
    }

    fun refreshObjectsTypes() {
        val selectedIndex = view.comboboxTypeObject.selectionModel.selectedIndex
        view.comboboxTypeObject.items = transaction {
            ExperimentObjectsType.all().toList().observable()
        }
        view.comboboxTypeObject.selectionModel.select(selectedIndex)
    }

    fun refreshMarks() {
        view.comboboxMark.items = transaction {
            MarksObjects.all().toList().observable()
        }
    }

    fun save() {
        if (isValuesDouble()) {
            transaction {
                ObjectsTypes.update({
                    ObjectsTypes.objectType eq view.comboboxTypeObject.selectionModel.selectedItem.objectType
                }) {
                    it[objectType] = view.comboboxTypeObject.selectionModel.selectedItem.objectType
                    it[insideD] = view.textFieldInside.text
                    it[outsideD] = view.textFieldOutside.text
                    it[ironLength] = view.textFieldIronLength.text
                    it[backHeight] = view.textFieldBackHeight.text
                    it[material] = view.comboBoxMaterial.selectionModel.selectedItem
                    it[insulation] = view.comboBoxInsulation.selectionModel.selectedItem
                }
            }
            refreshObjectsTypes()
        }
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
        if (!isValuesEmpty()) {
            val p = view.comboboxMark.value.density.toDouble()

            val ki: Double = if (view.comboBoxInsulation.selectionModel.selectedItem == "Лак") {
                0.93
            } else {
                0.95
            }
            insideD = view.textFieldInside.text.toDouble() //TODO может понадобится
            outsideD = view.textFieldOutside.text.toDouble()
            l = view.textFieldIronLength.text.toDouble()
            h = view.textFieldBackHeight.text.toDouble()
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

                view.textFieldU.text = String.format("%.2f", u)

                Toast.makeText("Выполнен расчет").show(Toast.ToastType.INFORMATION)
            }
        } else {
            Toast.makeText("Не все поля заполнены").show(Toast.ToastType.WARNING)
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
            view.vBoxEdit.isDisable = true
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
                appendOneMessageToLog("Проверьте подключение приборов к СУ")
                appendOneMessageToLog("Инициализация устройств")
                appendOneMessageToLog("Ожидайте...")
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

            Thread {
                isValuesNeed = true
                if (isExperimentRunning && isDevicesResponding) {
                    while (isValuesNeed && isExperimentRunning && isDevicesResponding) {
                        Platform.runLater {
                            view.textFieldU2.text = String.format("%.2f", CommunicationModel.u2)
                            view.textFieldU1.text = String.format("%.2f", CommunicationModel.u1)
                            view.textFieldI1.text = String.format("%.2f", CommunicationModel.i1)
                            view.textFieldP1.text = String.format("%.2f", CommunicationModel.p1)
                        }
                        Thread.sleep(10)
                    }
                }
            }.start()

            if (isExperimentRunning && !CommunicationModel.di1 && isDevicesResponding) {
                appendOneMessageToLog("Возврат в положение MIN")
                CommunicationModel.owenPR200Controller.onRegisterInKMS(DOWN)
            }

            while (isExperimentRunning && !CommunicationModel.di1 && isDevicesResponding) {
                Thread.sleep(1)
            }

            if (isExperimentRunning && isDevicesResponding) {
                CommunicationModel.owenPR200Controller.offRegisterInKMS(DOWN)
                appendOneMessageToLog("Латр в нижнем положении")
            }

            if (isExperimentRunning && isDevicesResponding) {
                appendOneMessageToLog("Собираем схему")
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
                appendOneMessageToLog("Поднимаем нужное напряжение...")
                CommunicationModel.owenPR200Controller.onRegisterInKMS(UP)
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
                appendOneMessageToLog("Началось измерение. Ожидайте...")
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


            appendOneMessageToLog("Возврат в положение MIN")
            CommunicationModel.owenPR200Controller.onRegisterInKMS(DOWN)
            while (!CommunicationModel.di1 && isDevicesResponding) {
                Thread.sleep(1)
            }
            if (isExperimentRunning && isDevicesResponding) {
                appendOneMessageToLog("Латр в нижнем положении")
            }

            CommunicationModel.owenPR200Controller.offRegisterInKMS(DOWN)

            CommunicationModel.owenPR200Controller.offAllKms()
            isExperimentRunning = false

            if (cause != "") {
                appendOneMessageToLog(String.format("Испытание прервано по причине: %s", cause))
            } else if (!isDevicesResponding) {
                appendOneMessageToLog("Испытание прервано: Потеряна связь с приборами")
                setCause("Потеряна связь с приборами")
            } else {
                appendOneMessageToLog("Испытание завершено успешно")
            }

            showResults()

            Platform.runLater {
                view.vBoxEdit.isDisable = false
                view.buttonCalculation.isDisable = false
                view.buttonStartExperiment.isDisable = false
                view.buttonStartExperiment.text = "Испытание"
            }
        }.start()
    }
}
