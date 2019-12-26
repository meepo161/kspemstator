package ru.avem.kspemstator.view

import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import ru.avem.kspemstator.controllers.AddExperimentObjectController
import ru.avem.kspemstator.controllers.MainViewController
import ru.avem.kspemstator.database.entities.ExperimentObject
import ru.avem.kspemstator.database.entities.ObjectsTable
import ru.avem.kspemstator.utils.Toast
import tornadofx.*

class AddExperimentObjectWindow : View("Добавить материал") {
    var textFieldMark: TextField by singleAssign()
    var textFieldDensity: TextField by singleAssign()
    var textfieldLosses: TextField by singleAssign()
    var textfieldIntensity: TextField by singleAssign()
    var tableViewObjects: TableView<ExperimentObject> by singleAssign()

    private val controller: AddExperimentObjectController by inject()
    private val mainController: MainViewController by inject()

    override fun onBeforeShow() {
        modalStage!!.setOnHiding {
            controller.refreshObjectsTable()
        }
    }

    override val root = anchorpane {

        hbox(spacing = 16.0) {
            tableViewObjects = tableview {
                columnResizePolicyProperty().set(TableView.CONSTRAINED_RESIZE_POLICY)
                prefWidth = 900.0
                items = controller.getObjects()

                column("Марка", ExperimentObject::mark) {
                    onEditCommit = EventHandler { cell ->
                        transaction {
                            ObjectsTable.update({
                                ObjectsTable.mark eq selectedItem!!.mark
                            }) {
                                it[mark] = cell.newValue
                            }
                        }
                    }
                }

                column("Плотность", ExperimentObject::density) {
                    onEditCommit = EventHandler { cell ->
                        transaction {
                            ObjectsTable.update({
                                ObjectsTable.density eq selectedItem!!.density
                            }) {
                                it[density] = cell.newValue
                            }
                        }
                    }
                }

                column("Потери", ExperimentObject::losses) {
                    onEditCommit = EventHandler { cell ->
                        transaction {
                            ObjectsTable.update({
                                ObjectsTable.losses eq selectedItem!!.losses
                            }) {
                                it[losses] = cell.newValue
                            }
                        }
                    }
                }

                column("Напряженность", ExperimentObject::intensity) {
                    onEditCommit = EventHandler { cell ->
                        transaction {
                            ObjectsTable.update({
                                ObjectsTable.intensity eq selectedItem!!.intensity
                            }) {
                                it[intensity] = cell.newValue
                            }
                        }
                    }
                }

            }
            vbox(spacing = 16.0) {
                anchorpaneConstraints {
                    leftAnchor = 16.0
                    rightAnchor = 16.0
                    topAnchor = 16.0
                    bottomAnchor = 16.0
                }

                alignmentProperty().set(Pos.CENTER)

                hbox(spacing = 16.0) {
                    alignmentProperty().set(Pos.CENTER_RIGHT)

                    label("Марка стали:")
                    textFieldMark = textfield {
                        prefWidth = 200.0
                    }
                }

                hbox(spacing = 16.0) {
                    alignmentProperty().set(Pos.CENTER_RIGHT)

                    label("Плотность стали, кг/м³:")
                    textFieldDensity = textfield {
                        prefWidth = 200.0
                    }
                }

                hbox(spacing = 16.0) {
                    alignmentProperty().set(Pos.CENTER_RIGHT)

                    label("Удельные потери, Вт/кг:")
                    textfieldLosses = textfield {
                        prefWidth = 200.0
                    }
                }

                hbox(spacing = 16.0) {
                    alignmentProperty().set(Pos.CENTER_RIGHT)

                    label("Напряженность, А/м")
                    textfieldIntensity = textfield {
                        prefWidth = 200.0
                    }
                }

                hbox(spacing = 16.0) {
                    alignmentProperty().set(Pos.CENTER)

                    button("Добавить") {
                        action {
                            if (controller.addObject()) {
                                Toast.makeText("Новый объект добавлен.").show(Toast.ToastType.INFORMATION)
                            }
                            controller.refreshObjectsTable()
                            mainController.refreshObjects()
                        }
                    }
                    button("Удалить") {
                        action {
                            controller.deleteObject()
                            controller.refreshObjectsTable()
                            mainController.refreshObjects()
                        }
                    }
                }
            }
        }
    }.addClass(Styles.medium)

}
