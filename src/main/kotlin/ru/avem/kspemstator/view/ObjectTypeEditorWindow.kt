package ru.avem.kspemstator.view

import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.control.ComboBox
import javafx.scene.control.TextInputDialog
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import ru.avem.kspemstator.controllers.MainViewController
import ru.avem.kspemstator.database.entities.ExperimentObjectsType
import ru.avem.kspemstator.database.entities.ObjectsTypes
import ru.avem.kspemstator.utils.Toast
import tornadofx.*

class ObjectTypeEditorWindow : View("Редактор типов двигателя") {

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
                alignmentProperty().set(Pos.TOP_CENTER)
                label("Выберите тип двигателя для удаления или добавьте новый")
            }
            hbox(spacing = 16.0) {
                alignmentProperty().set(Pos.TOP_CENTER)
                comboboxObjectsTypes = combobox {
                    prefWidth = 200.0
                    items = getObjects()
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

                                    val isSecondName = ExperimentObjectsType.find() {
                                        ObjectsTypes.objectType eq name
                                    }
                                    if (isSecondName.empty()) {
                                        ExperimentObjectsType.new {
                                            objectType = name
                                        }
                                        Toast.makeText("Новый тип добавлен.").show(Toast.ToastType.INFORMATION)
                                    } else {
                                        Toast.makeText("Такой тип двигателя уже существует.").show(Toast.ToastType.ERROR)
                                    }
                                }
                            }
                        }
                        comboboxObjectsTypes.items = getObjects()
                        mainController.refreshObjectsTypes()
                    }
                }
                button("Удалить") {
                    action {
                        deleteObject()
                        mainController.refreshObjectsTypes()
                        comboboxObjectsTypes.items = getObjects()
                    }
                }
            }
        }
    }.addClass(Styles.medium)


    fun getObjects(): ObservableList<ExperimentObjectsType> {
        return transaction {
            ExperimentObjectsType.all().toList().observable()
        }
    }

    fun deleteObject() {
        val item = comboboxObjectsTypes.selectionModel.selectedItem
        if (item != null) {
            transaction {
                ObjectsTypes.deleteWhere() {
                    ObjectsTypes.objectType eq item.objectType
                }
            }
        }
    }
}