package ru.avem.kspemstator.view

import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import javafx.stage.Modality
import org.jetbrains.exposed.sql.transactions.transaction
import ru.avem.kspemstator.database.entities.*
import ru.avem.kspemstator.view.Styles.Companion.medium
import tornadofx.*


class MainView : View("Испытание активной стали статора") {

    private var comboboxUserSelector: ComboBox<User> by singleAssign()

    private var textFieldTypeObject: TextField by singleAssign()
    private var textFieldFacNumber: TextField by singleAssign()
    private var textFieldOutside: TextField by singleAssign()
    private var textFieldInside: TextField by singleAssign()
    private var textFieldIronLength: TextField by singleAssign()
    private var textFieldBackHeight: TextField by singleAssign()


    private var comboboxMaterial: ComboBox<ExperimentObject> by singleAssign()
    private var comboboxMark: ComboBox<ExperimentObject> by singleAssign()
    private var comboboxTypeInsulation: ComboBox<ExperimentObject> by singleAssign()

    override fun onDock() {
        refreshUsers()
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
                    item("Объекты испытания") {

                    }
                    item("Сталь") {
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
                            showAboutUs()
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
                        label("Испытатель") {

                        }
                        comboboxUserSelector = combobox {
                            prefWidth = 200.0

                        }
                        label("") {

                        }
                        label("ПАРАМЕТРЫ ДВИГАТЕЛЯ") {

                        }
                        label("Тип двигателя:") {

                        }
                        textFieldFacNumber = textfield {
                        }
                        label("Номер двигателя:") {

                        }
                        textFieldTypeObject = textfield {

                        }
                        label("Наружный диаметр:") {

                        }
                        textFieldOutside = textfield {

                        }
                        label("Внутренний диаметр:") {

                        }
                        textFieldInside = textfield {

                        }
                        label("Длина железа:") {

                        }
                        textFieldIronLength = textfield {

                        }
                        label("Высота спинки:") {

                        }
                        textFieldBackHeight = textfield {

                        }
                        label("Материал станины:") {

                        }
                        comboboxMaterial = combobox {
                            prefWidth = 200.0

                        }
                        label("Марка стали:") {

                        }
                        comboboxMark = combobox {
                            prefWidth = 200.0

                        }
                        label("Тип изоляции:") {

                        }
                        comboboxTypeInsulation = combobox {
                            prefWidth = 200.0

                        }
                        button("Автовыбор стали") {

                            action {
                                refreshObjects()
                            }
                        }

                    }
                    vbox(spacing = 16.0) {
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
                                textarea {
                                    prefHeight = 300.0

                                }
                            }

                            vbox(spacing = 16.0) {
                                alignmentProperty().set(Pos.TOP_CENTER)
                                button("Испытание") {
                                    prefWidth = 300.0

                                }.addClass(Styles.hard)
                                textarea {
                                    prefHeight = 300.0

                                }
                            }
                        }
                    }
                }
            }
        }
    }.addClass(medium)

    private fun calculate() {

    }

    private fun clearTFs() {
        textFieldTypeObject.text = ""
        textFieldTypeObject.text = ""
        textFieldFacNumber.text = ""
        textFieldOutside.text = ""
        textFieldInside.text = ""
        textFieldIronLength.text = ""
        textFieldBackHeight.text = ""
    }

    private fun showAboutUs() {
        val alert = Alert(Alert.AlertType.INFORMATION)
        alert.title = "Версия ПО"
        alert.headerText = "Версия: 0.0.1b"
        alert.contentText = "Дата: 05.12.2019"
        alert.showAndWait()
    }

    private fun refreshUsers() {
        comboboxUserSelector.items = transaction {
            User.find {
                Users.login notLike "admin"
            }.toList().observable()
        }
    }

    private fun refreshObjects() {
        comboboxMaterial.items = transaction {
            ExperimentObject.find {
                ObjectsTable.material notLike ""
            }.toList().observable()
        }
    }
}
