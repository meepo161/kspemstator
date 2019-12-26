package ru.avem.kspemstator.view

import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.control.ComboBox
import javafx.scene.control.TextInputDialog
import org.jetbrains.exposed.sql.transactions.transaction
import ru.avem.kspemstator.controllers.MainViewController
import ru.avem.kspemstator.database.entities.ExperimentObjectsType
import ru.avem.kspemstator.utils.Toast
import tornadofx.*

class TypeEditorWindow : View("Редактор типов двигателя") {

    var comboboxObjectsTypes: ComboBox<ExperimentObjectsType> by singleAssign()
    private val mainController: MainViewController by inject()


    override val root = anchorpane {
        vbox(spacing = 16.0) {
            alignmentProperty().set(Pos.TOP_CENTER)
            anchorpaneConstraints {
                leftAnchor = 16.0
                rightAnchor = 16.0
                topAnchor = 16.0
                bottomAnchor = 16.0
            }
            hbox(spacing = 16.0) {
                label("Выберите тип двигателя для удаления или добавьте новый")
            }
            hbox(spacing = 16.0) {
                comboboxObjectsTypes = combobox {
                    prefWidth = 200.0
                }

            }
            hbox(spacing = 16.0) {
                alignmentProperty().set(Pos.CENTER)

                button("Добавить") {
                    action {
                        val dialog = TextInputDialog("Двигатель")
                        dialog.title = "Редактор типов двигателя"
                        dialog.headerText = "Добавить новый тип двигателя"
                        dialog.contentText = "Введите тип: "
                        val result = dialog.showAndWait()
                        if (result.isPresent) {
                            var name = result.get()
                            if (name.trim().isNotBlank()) {
                                transaction {
                                    ExperimentObjectsType.new {
                                        objectType = name
                                    }
                                }
                            }
                        }
                        Toast.makeText("Новый объект добавлен.").show(Toast.ToastType.INFORMATION)
                        comboboxObjectsTypes.items = getObjects()
                        mainController.refreshObjectsTypes()
                    }
                }
                button("Удалить") {
                    action {
                        comboboxObjectsTypes.items = getObjects()
                        mainController.refreshObjectsTypes()
                    }
                }
            }
        }
    }


    fun getObjects(): ObservableList<ExperimentObjectsType> {
        return transaction {
            ExperimentObjectsType.all().toList().observable()
        }
    }
}