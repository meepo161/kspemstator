package ru.avem.kspemstator.view

import javafx.application.Platform
import javafx.beans.property.SimpleIntegerProperty
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.control.ComboBox
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.VBox
import javafx.stage.Modality
import ru.avem.kspemstator.controllers.MainViewController
import ru.avem.kspemstator.database.entities.ExperimentObject
import ru.avem.kspemstator.database.entities.ExperimentObjectsType
import ru.avem.kspemstator.database.entities.User
import ru.avem.kspemstator.utils.Toast
import ru.avem.kspemstator.view.Styles.Companion.medium
import tornadofx.*


class MainView : View("Испытание активной стали статора") {

    private val controller: MainViewController by inject()

    var comboboxUserSelector: ComboBox<User> by singleAssign()

    var comboboxTypeObject: ComboBox<ExperimentObjectsType> by singleAssign()

    var textFieldFacNumber: TextField by singleAssign()
    var textFieldOutside: TextField by singleAssign()
    var textFieldInside: TextField by singleAssign()
    var textFieldIronLength: TextField by singleAssign()
    var textFieldBackHeight: TextField by singleAssign()

    var comboBoxMark: ComboBox<ExperimentObject> by singleAssign()
    var comboBoxMaterial: ComboBox<String> by singleAssign()
    var comboBoxInsulation: ComboBox<String> by singleAssign()

    private var vBoxMain: VBox by singleAssign()

    private var textAreaCalculate: TextArea by singleAssign()
    private var textAreaExperiment: TextArea by singleAssign()

    private val insulationList: ObservableList<String> = observableList("Лак", "Оксидирование")
    private val materialString: ObservableList<String> = observableList("Алюминий", "Сталь")

    private val sliderForward = SimpleIntegerProperty(50)
    private val sliderBack = SimpleIntegerProperty(50)

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

    override fun onDock() {
        comboBoxInsulation.items = insulationList
        comboBoxMaterial.items = materialString
        controller.refreshUsers()
        controller.refreshObjects()
        controller.refreshObjectsTypes()
    }

    override val root = borderpane {

        top {
            menubar {
                menu("Меню") {
                    item("Очистить") {
                        action {
                            clearTFs()
                        }
                    }
                }
                menu("База данных") {
                    item("Испытатели") {
                        action {
                            find<UserEditorWindow>().openModal(
                                modality = Modality.WINDOW_MODAL, escapeClosesWindow = true,
                                resizable = false, owner = this@MainView.currentWindow
                            )
                        }
                    }
                    item("Материал") {
                        action {
                            find<ExperimentObjectEditorWindow>().openModal(
                                modality = Modality.WINDOW_MODAL, escapeClosesWindow = true,
                                resizable = false, owner = this@MainView.currentWindow
                            )
                        }
                    }
                    item("Тип двигателя") {
                        action {
                            find<TypeEditorWindow>().openModal(
                                modality = Modality.WINDOW_MODAL, escapeClosesWindow = true,
                                resizable = false, owner = this@MainView.currentWindow
                            )
                        }
                    }
                    item("Протоколы") {

                    }
                }
                menu("Информация") {
                    item("Версия ПО") {
                        action {
                            controller.showAboutUs()
                        }
                    }
                }
            }
        }
        center {
            anchorpane {
                hbox(spacing = 16.0) {
                    alignmentProperty().set(Pos.TOP_CENTER)
                    anchorpaneConstraints {
                        leftAnchor = 16.0
                        rightAnchor = 16.0
                        topAnchor = 16.0
                        bottomAnchor = 16.0
                    }
                    vbox(spacing = 8.0) {
                        alignmentProperty().set(Pos.TOP_CENTER)
                        label("Испытатель")
                        comboboxUserSelector = combobox {
                            prefWidth = 200.0
                        }
                        label("Тип двигателя:")
                        textFieldFacNumber = textfield {

                        }
                        label("Номер двигателя:")
                        comboboxTypeObject = combobox {

                        }
                        label("Наружный диаметр:")
                        textFieldOutside = textfield {

                        }
                        label("Внутренний диаметр:")
                        textFieldInside = textfield {

                        }
                        label("Длина железа:")
                        textFieldIronLength = textfield {

                        }
                        label("Высота спинки:")
                        textFieldBackHeight = textfield {

                        }
                        label("Материал станины:")
                        comboBoxMaterial = combobox {
                            prefWidth = 200.0

                        }
                        label("Марка стали:")
                        comboBoxMark = combobox {
                            prefWidth = 200.0

                        }
                        label("Тип изоляции:")
                        comboBoxInsulation = combobox {
                            prefWidth = 200.0

                        }
                        label(sliderForward.stringBinding {
                            "ШИМ вперед: ${sliderForward.value}%"
                        })
                        slider(0, 100, 1) {
                            isShowTickLabels = true
                            isShowTickMarks = true
                            valueProperty().bindBidirectional(sliderForward)
                        }
                        label(sliderBack.stringBinding {
                            "ШИМ назад: ${sliderBack.value}%"
                        })
                        slider(0, 100, 1) {
                            isShowTickLabels = true
                            isShowTickMarks = true
                            valueProperty().bindBidirectional(sliderBack)
                        }
                    }
                    vBoxMain = vbox(spacing = 16.0) {
                        alignmentProperty().set(Pos.TOP_CENTER)
                        hbox(spacing = 16.0) {
                            vbox(spacing = 16.0) {
                                alignmentProperty().set(Pos.TOP_CENTER)
                                button("Расчет") {
                                    prefWidth = 300.0

                                    action {
                                        calculate()
                                    }
                                }.addClass(Styles.hard)
                                textAreaCalculate = textarea {
                                    prefHeight = 760.0

                                }
                            }

                            vbox(spacing = 16.0) {
                                alignmentProperty().set(Pos.TOP_CENTER)
                                button("Испытание") {
                                    prefWidth = 300.0

                                }.addClass(Styles.hard)
                                textAreaExperiment = textarea {
                                    prefHeight = 760.0

                                }
                            }
                        }
                    }
                }
            }
        }
    }.addClass(medium)

    private fun calculate() {

//        textFieldTypeObject.text = "1111111"
//        textFieldFacNumber.text = "2222222"
//        textFieldOutside.text = "3333333"
//        textFieldInside.text = "2222222"
//        textFieldBackHeight.text = "22"
//        textFieldIronLength.text = "11"

        if (!controller.isValuesEmpty()) {
            val p = comboBoxMark.selectionModel.selectedItem.density.toDouble()

            val ki: Double = if (comboBoxInsulation.selectionModel.selectedItem == "Лак") {
                0.93
            } else {
                0.95
            }
            insideD = textFieldInside.text.toDouble() //TODO может понадобится
            outsideD = textFieldOutside.text.toDouble()
            l = textFieldIronLength.text.toDouble()
            h = textFieldBackHeight.text.toDouble()
            lakt = ki * l * 0.001
            lsr = Math.PI * (outsideD - h) * 0.001
            sh = lakt * h * 0.001
            v = lsr * sh
            m = v * p
            u = 4.44 * 21 * 50 * sh
            Platform.runLater {
                textAreaCalculate.text = "1.Длина активной стали (Lакт) в метрах:" +
                        "\nLакт = " + String.format("%.4f м.", lakt) +
                        "\n2.Длина средней линии железа (Lср) в метрах:" +
                        "\nLср = " + String.format("%.4f м.", lsr) +
                        "\n3.Сечение спинки Sh (м²)" +
                        "\nSh = " + String.format("%.4f м².", sh) +
                        "\n4.Объем железа (V) (м³)" +
                        "\nV = " + String.format("%.4f м³.", v) +
                        "\n5.Масса железа (M) (кг)" +
                        "\nM = " + String.format("%.4f кг.", m) +
                        "\n6.Расчетное напряжение Uo" +
                        "\nUo = " + String.format("%.4f В.", u)

                Toast.makeText("Выполнен расчет").show(Toast.ToastType.INFORMATION)
            }
        } else {
            Toast.makeText("Неверно заполнены поля").show(Toast.ToastType.WARNING)
        }
    }

    private fun clearTFs() {
        comboboxTypeObject.selectionModel.select(0)
        textFieldFacNumber.text = ""
        textFieldOutside.text = ""
        textFieldInside.text = ""
        textFieldIronLength.text = ""
        textFieldBackHeight.text = ""
        comboBoxMark.selectionModel.select(0)
        comboBoxMaterial.selectionModel.select(0)
        comboBoxInsulation.selectionModel.select(0)
    }
}
