package ru.avem.kspemstator.view

import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.ComboBox
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import ru.avem.kspemstator.controllers.ObjectTypeEditorController
import ru.avem.kspemstator.database.entities.ExperimentObjectsType
import ru.avem.kspemstator.database.entities.MarksObjects
import ru.avem.kspemstator.database.entities.ObjectsTypes
import ru.avem.kspemstator.utils.Toast
import tornadofx.*
import java.awt.Desktop
import java.nio.file.Paths

class ObjectTypeEditorWindow : View("Добавить материал") {

    var textfieldObjectType: TextField by singleAssign()
    var textfieldPower: TextField by singleAssign()
    var textfieldFrequency: TextField by singleAssign()
    var textfieldInside: TextField by singleAssign()
    var textfieldOutside: TextField by singleAssign()
    var textfieldIronLength: TextField by singleAssign()
    var textfieldBackHeight: TextField by singleAssign()

    var comboBoxMaterial: ComboBox<String> by singleAssign()
    var comboBoxInsulation: ComboBox<String> by singleAssign()
    var comboBoxMark: ComboBox<MarksObjects> by singleAssign()

    var tableViewObjects: TableView<ExperimentObjectsType> by singleAssign()

    private val controller: ObjectTypeEditorController by inject()
    private val mainView: MainView by inject()

    private val insulationList: ObservableList<String> = observableList("Лак", "Оксидирование")
    private val materialString: ObservableList<String> = observableList("Алюминий", "Сталь")

    override fun onBeforeShow() {
        modalStage!!.setOnHiding {
            //            controller.refreshObjectsTable()
        }
    }

    override fun onDock() {
        super.onDock()
        refreshMarks()
        comboBoxInsulation.items = insulationList
        comboBoxMaterial.items = materialString
        comboBoxMaterial.selectionModel.selectFirst()
        comboBoxInsulation.selectionModel.selectFirst()
        comboBoxMark.selectionModel.selectFirst()
    }

    fun refreshMarks() {
        comboBoxMark.items = transaction {
            MarksObjects.all().toList().observable()
        }
    }

    override val root = anchorpane {
        prefWidth = 1280.0
        maxHeight = 780.0
        hbox(spacing = 16.0) {
            anchorpaneConstraints {
                leftAnchor = 16.0
                rightAnchor = 16.0
                topAnchor = 16.0
                bottomAnchor = 16.0
            }
            alignment = Pos.CENTER

            tableViewObjects = tableview {
                columnResizePolicyProperty().set(TableView.CONSTRAINED_RESIZE_POLICY)
                prefWidth = 1000.0
                items = controller.getObjects()
                column("Тип", ExperimentObjectsType::objectType) {
                    onEditCommit = EventHandler { cell ->
                        transaction {
                            ObjectsTypes.update({
                                ObjectsTypes.objectType eq selectedItem!!.objectType
                            }) {
                                it[objectType] = cell.newValue
                            }
                        }
                    }
                }.addClass(Styles.medium)

                column("Мощность, кВт", ExperimentObjectsType::power) {
                    onEditCommit = EventHandler { cell ->
                        transaction {
                            ObjectsTypes.update({
                                ObjectsTypes.power eq selectedItem!!.power
                            }) {
                                it[power] = cell.newValue
                            }
                        }
                    }
                }.addClass(Styles.medium)

                column("Частота\nвращения", ExperimentObjectsType::frequency) {
                    onEditCommit = EventHandler { cell ->
                        transaction {
                            ObjectsTypes.update({
                                ObjectsTypes.frequency eq selectedItem!!.frequency
                            }) {
                                it[frequency] = cell.newValue
                            }
                        }
                    }
                }.addClass(Styles.medium)

                column("Наружный\nдиаметр, мм", ExperimentObjectsType::outsideD) {
                    onEditCommit = EventHandler { cell ->
                        transaction {
                            ObjectsTypes.update({
                                ObjectsTypes.outsideD eq selectedItem!!.outsideD
                            }) {
                                it[outsideD] = cell.newValue
                            }
                        }
                    }
                }.addClass(Styles.medium)

                column("Внутренний\nдиаметр", ExperimentObjectsType::insideD) {
                    onEditCommit = EventHandler { cell ->
                        transaction {
                            ObjectsTypes.update({
                                ObjectsTypes.insideD eq selectedItem!!.insideD
                            }) {
                                it[insideD] = cell.newValue
                            }
                        }
                    }
                }.addClass(Styles.medium)

                column("Длина", ExperimentObjectsType::ironLength) {
                    onEditCommit = EventHandler { cell ->
                        transaction {
                            ObjectsTypes.update({
                                ObjectsTypes.ironLength eq selectedItem!!.ironLength
                            }) {
                                it[ironLength] = cell.newValue
                            }
                        }
                    }
                }.addClass(Styles.medium)

                column("Высота\nспинки", ExperimentObjectsType::backHeight) {
                    onEditCommit = EventHandler { cell ->
                        transaction {
                            ObjectsTypes.update({
                                ObjectsTypes.backHeight eq selectedItem!!.backHeight
                            }) {
                                it[backHeight] = cell.newValue
                            }
                        }
                    }
                }.addClass(Styles.medium)

                column("Материал", ExperimentObjectsType::material) {
                    onEditCommit = EventHandler { cell ->
                        transaction {
                            ObjectsTypes.update({
                                ObjectsTypes.material eq selectedItem!!.material
                            }) {
                                it[material] = cell.newValue
                            }
                        }
                    }
                }.addClass(Styles.medium)

                column("Изоляция", ExperimentObjectsType::insulation) {
                    onEditCommit = EventHandler { cell ->
                        transaction {
                            ObjectsTypes.update({
                                ObjectsTypes.insulation eq selectedItem!!.insulation
                            }) {
                                it[insulation] = cell.newValue
                            }
                        }
                    }
                }.addClass(Styles.medium)

                column("Марка", ExperimentObjectsType::mark) {
                    onEditCommit = EventHandler { cell ->
                        transaction {
                            ObjectsTypes.update({
                                ObjectsTypes.mark eq selectedItem!!.mark
                            }) {
                                it[mark] = cell.newValue
                            }
                        }
                    }
                }.addClass(Styles.medium)
            }
            vbox(spacing = 0.0) {
                anchorpaneConstraints {
                    leftAnchor = 4.0
                    rightAnchor = 4.0
                    topAnchor = 4.0
                    bottomAnchor = 4.0
                }

                alignmentProperty().set(Pos.CENTER)
                vbox(spacing = 0.0) {
                    alignmentProperty().set(Pos.CENTER)

                    label("Тип двигателя:")
                    textfieldObjectType = textfield {
                        prefWidth = 200.0
                        maxWidth = 200.0
                        callKeyBoard()
                    }.addClass(Styles.medium)
                }

                vbox(spacing = 0.0) {
                    alignmentProperty().set(Pos.CENTER)

                    label("Мощность двигателя, кВт:")
                    textfieldPower = textfield {
                        prefWidth = 200.0
                        maxWidth = 200.0
                        callKeyBoard()
                    }.addClass(Styles.medium)
                }

                vbox(spacing = 0.0) {
                    alignmentProperty().set(Pos.CENTER)

                    label("Частота вращения:")
                    textfieldFrequency = textfield {
                        prefWidth = 200.0
                        maxWidth = 200.0
                        callKeyBoard()
                    }.addClass(Styles.medium)
                }

                vbox(spacing = 0.0) {
                    alignmentProperty().set(Pos.CENTER)

                    label("Наружный диаметр, мм:")
                    textfieldOutside = textfield {
                        prefWidth = 200.0
                        maxWidth = 200.0
                        callKeyBoard()
                    }.addClass(Styles.medium)
                }

                vbox(spacing = 0.0) {
                    alignmentProperty().set(Pos.CENTER)

                    label("Внутренний диаметр, мм:")
                    textfieldInside = textfield {
                        prefWidth = 200.0
                        maxWidth = 200.0
                        callKeyBoard()
                    }.addClass(Styles.medium)
                }

                vbox(spacing = 0.0) {
                    alignmentProperty().set(Pos.CENTER)

                    label("Длина железа, мм:")
                    textfieldIronLength = textfield {
                        prefWidth = 200.0
                        maxWidth = 200.0
                        callKeyBoard()
                    }.addClass(Styles.medium)
                }
                vbox(spacing = 0.0) {
                    alignmentProperty().set(Pos.CENTER)

                    label("Высота спинки, мм:")
                    textfieldBackHeight = textfield {
                        prefWidth = 200.0
                        maxWidth = 200.0
                        callKeyBoard()
                    }.addClass(Styles.medium)
                }
                vbox(spacing = 0.0) {
                    alignmentProperty().set(Pos.CENTER)
                    label("Материал станины:")
                    comboBoxMaterial = combobox {
                        prefWidth = 200.0

                    }
                }
                vbox(spacing = 0.0) {
                    alignmentProperty().set(Pos.CENTER)
                    label("Тип изоляции:")
                    comboBoxInsulation = combobox {
                        prefWidth = 200.0
                    }
                }
                vbox(spacing = 0.0) {
                    alignmentProperty().set(Pos.CENTER)
                    label("Марка")
                    comboBoxMark = combobox {
                        prefWidth = 200.0
                    }
                }
                vbox(spacing = 0.0) {
                    alignmentProperty().set(Pos.CENTER)
                    button("Автовыбор стали") {
                        prefWidth = 200.0
                        action {
                            if (!textfieldPower.text.isNullOrEmpty()) {
                                when {
                                    textfieldPower.text.toDouble() < 20 -> {
                                        comboBoxMark.selectionModel.select(1)
                                    }
                                    textfieldPower.text.toDouble() < 100 -> {
                                        comboBoxMark.selectionModel.select(0)
                                    }
                                }
                            }
                        }
                    }
                }

                hbox(spacing = 16.0) {
                    alignmentProperty().set(Pos.CENTER)

                    button("Добавить") {
                        action {
                            if (controller.addObject()) {
                                Toast.makeText("Новый объект добавлен.").show(Toast.ToastType.INFORMATION)
                            }
                            controller.refreshMarksTypes()
                        }
                    }
                    button("Удалить") {
                        action {
                            controller.deleteObject()
                            controller.refreshMarksTypes()
                        }
                    }
                }
            }
        }.addClass(Styles.medium)
    }.addClass(Styles.medium, Styles.baseColorFoo)

    fun TextField.callKeyBoard() {
        onTouchPressed = EventHandler {
            Desktop.getDesktop()
                .open(Paths.get("C:/Program Files/Common Files/Microsoft Shared/ink/TabTip.exe").toFile())
            requestFocus()
        }
    }
}
