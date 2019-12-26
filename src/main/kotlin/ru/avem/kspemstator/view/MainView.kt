package ru.avem.kspemstator.view

import javafx.application.Platform
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.control.ComboBox
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.VBox
import javafx.stage.Modality
import ru.avem.kspemstator.controllers.MainViewController
import ru.avem.kspemstator.database.entities.ExperimentObject
import ru.avem.kspemstator.database.entities.User
import ru.avem.kspemstator.utils.Toast
import ru.avem.kspemstator.view.Styles.Companion.medium
import tornadofx.*


class MainView : View("Испытание активной стали статора") {

    private val controller: MainViewController by inject()

    var comboboxUserSelector: ComboBox<User> by singleAssign()

    var textFieldTypeObject: TextField by singleAssign()
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

    private val insulationList: ObservableList<String> = observableList("Лак", "Окидирование")
    private val materialString: ObservableList<String> = observableList("Алюминий", "Сталь")

    override fun onDock() {
        comboBoxInsulation.items = insulationList
        comboBoxMaterial.items = materialString
        controller.refreshUsers()
        controller.refreshObjects()
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
                            find<AddExperimentObjectWindow>().openModal(
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
                        label("")
                        label("ПАРАМЕТРЫ ДВИГАТЕЛЯ")
                        label("Тип двигателя:")
                        textFieldFacNumber = textfield {

                        }
                        label("Номер двигателя:")
                        textFieldTypeObject = textfield {

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
                        label("Марка материала:")
                        comboBoxMark = combobox {
                            prefWidth = 200.0

                        }
                        label("Тип изоляции:")
                        comboBoxInsulation = combobox {
                            prefWidth = 200.0

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
                                    prefHeight = 600.0

                                }
                            }

                            vbox(spacing = 16.0) {
                                alignmentProperty().set(Pos.TOP_CENTER)
                                button("Испытание") {
                                    prefWidth = 300.0

                                }.addClass(Styles.hard)
                                textAreaExperiment = textarea {
                                    prefHeight = 600.0

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
            val insideD: Double = textFieldInside.text.toDouble()
            val outsideD: Double = textFieldOutside.text.toDouble()
            val l: Double = textFieldIronLength.text.toDouble()
            val h: Double = textFieldBackHeight.text.toDouble()
            val lakt: Double = ki * l * 0.001
            val lsr: Double = Math.PI * (outsideD - h) * 0.001
            val sh: Double = lakt * h * 0.001
            val v: Double = lsr * sh
            val m: Double = v * p
            val u: Double = 0.0
            Platform.runLater {
                textAreaCalculate.text = "1.Длина активной стали (Lакт) в метрах:"
                textAreaCalculate.text += "\nLакт = ki * L * 10-³ = " + String.format("%.4f м.", lakt)
                textAreaCalculate.text += "\n2.Длина средней линии железа (Lср) в метрах:"
                textAreaCalculate.text += "\nLср = π(D - h) * 10-³ = " + String.format("%.4f м.", lsr)
                textAreaCalculate.text += "\n3.Сечение спинки Sh (м²)"
                textAreaCalculate.text += "\nSh = Lакт * h * 10-³ = " + String.format("%.4f м².", sh)
                textAreaCalculate.text += "\n4.Объем железа (V) (м³)"
                textAreaCalculate.text += "\nV = Lср * Sh = " + String.format("%.4f м³.", v)
                textAreaCalculate.text += "\n5.Масса железа (V) (кг)"
                textAreaCalculate.text += "\nM = V * p = " + String.format("%.4f кг.", m)
                textAreaCalculate.text += "\n6.Расчетное напряжение Uo"
                textAreaCalculate.text += "\nUo = 250 * W * Bo * Sh * kr= " + String.format("%.4f В.", u)

                Toast.makeText("Выполнен расчет").show(Toast.ToastType.INFORMATION)
            }
        } else {
            Toast.makeText("Неверно заполнены поля").show(Toast.ToastType.WARNING)
        }
    }

    private fun clearTFs() {
        textFieldTypeObject.text = ""
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
