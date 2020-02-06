package ru.avem.kspemstator.view

import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Modality
import org.jetbrains.exposed.sql.transactions.transaction
import ru.avem.kspemstator.controllers.MainViewController
import ru.avem.kspemstator.database.entities.ExperimentObjectsType
import ru.avem.kspemstator.database.entities.User
import ru.avem.kspemstator.utils.Toast
import ru.avem.kspemstator.utils.callKeyBoard
import ru.avem.kspemstator.view.Styles.Companion.medium
import tornadofx.*
import java.awt.Desktop
import java.nio.file.Paths
import kotlin.system.exitProcess


class MainView : View("Испытание активной стали статора") {

    private val controller: MainViewController by inject()

    var comboboxUserSelector: ComboBox<User> by singleAssign()
    var mainMenubar: MenuBar by singleAssign()

    var comboboxTypeObject: ComboBox<ExperimentObjectsType> by singleAssign()

    var textFieldFacNumber: TextField by singleAssign()

    private var vBoxMain: VBox by singleAssign()
    private var hBoxResult: HBox by singleAssign()
    var hboxEdit: HBox by singleAssign()

    var textAreaExperiment: TextArea by singleAssign()

    var buttonStartExperiment: Button by singleAssign()

    private lateinit var currentItem: ExperimentObjectsType

    private var addIcon = ImageView("ru/avem/kspemstator/icon/add.png")
    private var deleteIcon = ImageView("ru/avem/kspemstator/icon/delete.png")
    private var editIcon = ImageView("ru/avem/kspemstator/icon/edit.png")

    override fun onBeforeShow() {
        addIcon.fitHeight = 16.0
        addIcon.fitWidth = 16.0
        deleteIcon.fitHeight = 16.0
        deleteIcon.fitWidth = 16.0
        editIcon.fitHeight = 16.0
        editIcon.fitWidth = 16.0
    }

    override fun onDock() {
        controller.refreshObjectsTypes()
    }

    override val root = borderpane {
        maxWidth = 1280.0
        maxHeight = 800.0
        top {
            mainMenubar = menubar {
                menu("Меню") {
                    item("Очистить") {
                        action {
                            clearFields()
                        }
                    }
                    item("Выход") {
                        action {
                            exitProcess(0)
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
                    item("Типы двигателя") {
                        action {
                            find<ObjectTypeEditorWindow>().openModal(
                                modality = Modality.WINDOW_MODAL, escapeClosesWindow = true,
                                resizable = false, owner = this@MainView.currentWindow
                            )
                        }
                    }
                    item("Марки стали") {
                        action {
                            find<AddMarkWindow>().openModal(
                                modality = Modality.WINDOW_MODAL, escapeClosesWindow = true,
                                resizable = false, owner = this@MainView.currentWindow
                            )
                        }
                    }
                    item("Протоколы") {
                        action {
                            find<ProtocolListWindow>().openModal(
                                modality = Modality.APPLICATION_MODAL, escapeClosesWindow = true,
                                resizable = false, owner = this@MainView.currentWindow
                            )
                        }
                    }
                }
                menu("Информация") {
                    item("Версия ПО") {
                        action {
                            controller.showAboutUs()
                        }
                    }
                }
            }.addClass(Styles.megaHard)
        }
        center {
            anchorpane {
                vbox(spacing = 32.0) {
                    anchorpaneConstraints {
                        leftAnchor = 16.0
                        rightAnchor = 16.0
                        topAnchor = 16.0
                        bottomAnchor = 16.0
                    }
                    alignmentProperty().set(Pos.CENTER)
                    hboxEdit = hbox(spacing = 16.0) {
                        minWidth = 360.0
                        prefWidth = 360.0
                        alignmentProperty().set(Pos.CENTER)

                        label("Тип двигателя:")
                        comboboxTypeObject = combobox {
                            prefWidth = 360.0
                        }
                        label("Номер двигателя:")
                        textFieldFacNumber = textfield {
                            prefWidth = 360.0
                            callKeyBoard()
                        }
                    }.addClass(Styles.extraHard)
                    hbox(spacing = 16.0) {
                        anchorpaneConstraints {
                            leftAnchor = 16.0
                            rightAnchor = 16.0
                            topAnchor = 16.0
                            bottomAnchor = 16.0
                        }
                        alignment = Pos.CENTER

                        vBoxMain = vbox(spacing = 16.0) {
                            alignmentProperty().set(Pos.CENTER)

                            buttonStartExperiment = button("Испытание") {
                                isDefaultButton = true
                                prefWidth = 900.0
                                prefHeight = 200.0
                                action {
                                    if (comboboxTypeObject.selectionModel.selectedItem == null) {
                                        Toast.makeText("Выберите тип двигателя").show(Toast.ToastType.WARNING)
                                    } else if (textFieldFacNumber.text.isNullOrEmpty()) {
                                        Toast.makeText("Введите номер двигателя").show(Toast.ToastType.WARNING)
                                    } else {
                                        if (controller.isExperimentRunning) {
                                            controller.stopExperiment()
                                        } else {
                                            controller.startExperiment()
                                        }
                                    }
                                }
                            }.addClass(Styles.stopStart)

                            textAreaExperiment = textarea {
                                prefHeight = 600.0
                                prefWidth = 1800.0
                            }.addClass(Styles.hard)
                        }
                    }
                }
            }
        }
    }.addClass(Styles.baseColorFoo, medium)

    private fun clearFields() {
        comboboxTypeObject.selectionModel.select(0)
        textFieldFacNumber.text = ""
        textAreaExperiment.text = ""
    }

    fun handleAddTestItem() {
        val dialog = TextInputDialog("Двигатель")
        dialog.title = "Редактор типов двигателя"
        dialog.headerText = "Добавить новый тип двигателя"
        dialog.contentText = "Введите тип: "
        Desktop.getDesktop()
            .open(Paths.get("C:/Program Files/Common Files/Microsoft Shared/ink/TabTip.exe").toFile())
        val result = dialog.showAndWait()
        if (result.isPresent) {
            val name = result.get()
            if (name.trim().isNotBlank()) {
                transaction {
                    ExperimentObjectsType.new {
                        objectType = name
                        outsideD = "1"
                        insideD = "1"
                        ironLength = "1"
                        backHeight = "1"
                        material = "Сталь"
                        insulation = "Лак"
                    }
                }
            } else {
                Toast.makeText("Введите корректное наименование типа").show(Toast.ToastType.INFORMATION)
            }
        }
        controller.refreshObjectsTypes()
    }
}
