package ru.avem.kspemstator.view

import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Modality
import org.jetbrains.exposed.sql.transactions.transaction
import ru.avem.kspemstator.controllers.MainViewController
import ru.avem.kspemstator.database.entities.ExperimentObjectsType
import ru.avem.kspemstator.database.entities.MarksObjects
import ru.avem.kspemstator.database.entities.User
import ru.avem.kspemstator.utils.Toast
import ru.avem.kspemstator.view.Styles.Companion.medium
import tornadofx.*
import java.awt.Desktop
import java.nio.file.Paths
import kotlin.system.exitProcess


class MainView : View("Испытание активной стали статора") {

    private val controller: MainViewController by inject()

    var comboboxUserSelector: ComboBox<User> by singleAssign()

    var comboboxTypeObject: ComboBox<ExperimentObjectsType> by singleAssign()

    var textFieldFacNumber: TextField by singleAssign()
    var textFieldOutside: TextField by singleAssign()
    var textFieldInside: TextField by singleAssign()
    var textFieldIronLength: TextField by singleAssign()
    var textFieldBackHeight: TextField by singleAssign()
    var comboboxMark: ComboBox<MarksObjects> by singleAssign()

    var comboBoxMaterial: ComboBox<String> by singleAssign()
    var comboBoxInsulation: ComboBox<String> by singleAssign()

    private var vBoxMain: VBox by singleAssign()
    private var hBoxResult: HBox by singleAssign()
    var vBoxEdit: VBox by singleAssign()

    var textAreaCalculate: TextArea by singleAssign()
    var textAreaExperiment: TextArea by singleAssign()
    var textAreaTotalInfo: TextArea by singleAssign()
    var textAreaResults: TextArea by singleAssign()

    var buttonStartExperiment: Button by singleAssign()
    var buttonCalculation: Button by singleAssign()

    private val insulationList: ObservableList<String> = observableList("Лак", "Оксидирование")
    private val materialString: ObservableList<String> = observableList("Алюминий", "Сталь")

    private lateinit var currentItem: ExperimentObjectsType

    private var addIcon = ImageView("ru/avem/kspemstator/icon/add.png")
    private var deleteIcon = ImageView("ru/avem/kspemstator/icon/delete.png")
    private var editIcon = ImageView("ru/avem/kspemstator/icon/edit.png")


    var textFieldU: TextField by singleAssign()
    var textFieldU2: TextField by singleAssign()
    var textFieldU1: TextField by singleAssign()
    var textFieldI1: TextField by singleAssign()
    var textFieldP1: TextField by singleAssign()

    override fun onBeforeShow() {
        addIcon.fitHeight = 16.0
        addIcon.fitWidth = 16.0
        deleteIcon.fitHeight = 16.0
        deleteIcon.fitWidth = 16.0
        editIcon.fitHeight = 16.0
        editIcon.fitWidth = 16.0
    }

    override fun onDock() {
        comboBoxInsulation.items = insulationList
        comboBoxMaterial.items = materialString
        controller.refreshUsers()
        controller.refreshObjectsTypes()
        controller.refreshMarks()
    }

    override val root = borderpane {
        maxWidth = 1920.0
        maxHeight = 1200.0
        top {
            menubar {
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
                    alignmentProperty().set(Pos.CENTER)
                    anchorpaneConstraints {
                        leftAnchor = 16.0
                        rightAnchor = 16.0
                        topAnchor = 16.0
                        bottomAnchor = 16.0
                    }
                    vBoxEdit = vbox(spacing = 4.0) {
                        minWidth = 300.0
                        prefWidth = 300.0
                        alignmentProperty().set(Pos.TOP_CENTER)
                        label("Испытатель")
                        comboboxUserSelector = combobox {
                            prefWidth = 240.0
                        }
                        label("Тип двигателя:")
                        comboboxTypeObject = combobox {
                            prefWidth = 240.0
//                            isEditable = true
                            onAction = EventHandler {
                                fillFields(selectedItem ?: items.first())
                            }
                        }
                        hbox(spacing = 16.0) {
                            alignmentProperty().set(Pos.TOP_CENTER)
                            button("", addIcon) {
                                action {
                                    handleAddTestItem()
                                }
                            }
                            button("", deleteIcon) {
                                action {
                                    controller.deleteTestItem()
                                }
                            }

                        }.addClass(Styles.medium)
                        label("Номер двигателя:")
                        textFieldFacNumber = textfield {
                            prefWidth = 240.0
                            maxWidth = 240.0
                            callKeyBoard()
                        }.addClass(Styles.medium)
                        label("Наружный диаметр:")
                        textFieldOutside = textfield {
                            prefWidth = 240.0
                            maxWidth = 240.0
                            callKeyBoard()
                        }.addClass(Styles.medium)
                        label("Внутренний диаметр:")
                        textFieldInside = textfield {
                            prefWidth = 240.0
                            maxWidth = 240.0
                            callKeyBoard()
                        }.addClass(Styles.medium)
                        label("Длина железа:")
                        textFieldIronLength = textfield {
                            prefWidth = 240.0
                            maxWidth = 240.0
                            callKeyBoard()
                        }.addClass(Styles.medium)
                        label("Высота спинки:")
                        textFieldBackHeight = textfield {
                            prefWidth = 240.0
                            maxWidth = 240.0
                            callKeyBoard()
                        }.addClass(Styles.medium)
                        label("Материал станины:")
                        comboBoxMaterial = combobox {
                            prefWidth = 240.0

                        }
                        label("Марка стали:")
                        hbox(spacing = 4.0) {
                            alignment = Pos.CENTER
                            comboboxMark = combobox<MarksObjects> {
                                prefWidth = 190.0
                                maxWidth = 190.0
                            }.addClass(Styles.medium)
                            button("", editIcon) {
                                action {
                                    find<AddMarkWindow>().openModal(
                                        modality = Modality.WINDOW_MODAL, escapeClosesWindow = true,
                                        resizable = false, owner = this@MainView.currentWindow
                                    )
                                }
                            }
                        }
                        label("Тип изоляции:")
                        comboBoxInsulation = combobox {
                            prefWidth = 240.0
                        }
                        button("Автовыбор стали") {
                            prefWidth = 240.0

                        }
                        button("Сохранить") {
                            prefWidth = 240.0
                            action {
                                controller.save()
                            }
                        }

//                        hbox(spacing = 4.0) {
//                            alignmentProperty().set(Pos.TOP_CENTER)
//                            button("ВВЕРХ") {
//                                action {
//                                    CommunicationModel.owenPR200Controller.onRegisterInKMS(UP)
//                                }
//                            }
//                            button("ВНИЗ") {
//                                action {
//                                    CommunicationModel.owenPR200Controller.onRegisterInKMS(DOWN)
//                                }
//                            }
//                            button("СТОП") {
//                                action {
//                                    CommunicationModel.owenPR200Controller.offRegisterInKMS(UP)
//                                    CommunicationModel.owenPR200Controller.offRegisterInKMS(DOWN)
//                                }
//                            }
//                        }


//                        label(sliderForward.stringBinding {
//                            "ШИМ вперед: ${sliderForward.value}%"
//                        })
//                        slider(0, 100, 1) {
//                            isShowTickLabels = true
//                            isShowTickMarks = true
//                            valueProperty().bindBidirectional(sliderForward)
//                        }
//                        label(sliderBack.stringBinding {
//                            "ШИМ назад: ${sliderBack.value}%"
//                        })
//                        slider(0, 100, 1) {
//                            isShowTickLabels = true
//                            isShowTickMarks = true
//                            valueProperty().bindBidirectional(sliderBack)
//                        }
                    }
                    vBoxMain = vbox(spacing = 16.0) {
                        alignmentProperty().set(Pos.TOP_CENTER)
                        hbox(spacing = 32.0) {
                            vbox(spacing = 16.0) {
                                alignmentProperty().set(Pos.TOP_CENTER)
                                buttonCalculation = button("Расчет") {
                                    prefWidth = 300.0
                                    action {
                                        controller.calculate()
                                    }
                                }.addClass(Styles.hard)
                                textAreaCalculate = textarea {
                                    prefHeight = 440.0
                                }.addClass(Styles.hard)
                            }
                            vbox(spacing = 16.0) {
                                alignmentProperty().set(Pos.TOP_CENTER)
                                buttonStartExperiment = button("Испытание") {
                                    prefWidth = 300.0
                                    action {
                                        if (controller.isExperimentRunning) {
                                            controller.stopExperiment()
                                        } else {
                                            controller.startExperiment()
                                        }
                                    }
                                }.addClass(Styles.hard)
                                hbox(spacing = 16.0) {
                                    alignmentProperty().set(Pos.CENTER)
                                    vbox(spacing = 4.0) {
                                        alignmentProperty().set(Pos.TOP_CENTER)
                                        label("Uo расч, В") {
                                        }.addClass(Styles.hard)
                                        textFieldU = textfield {
                                            maxWidth = 100.0
                                            alignmentProperty().set(Pos.CENTER)
                                        }.addClass(Styles.hard)
                                    }
                                    vbox(spacing = 4.0) {
                                        alignmentProperty().set(Pos.TOP_CENTER)
                                        label("Ui изм, В") {
                                        }.addClass(Styles.hard)
                                        textFieldU2 = textfield {
                                            maxWidth = 100.0
                                        }.addClass(Styles.hard)
                                    }
                                    vbox(spacing = 4.0) {
                                        alignmentProperty().set(Pos.TOP_CENTER)
                                        label("Uн намаг, В") {
                                        }.addClass(Styles.hard)
                                        textFieldU1 = textfield {
                                            maxWidth = 100.0
                                        }.addClass(Styles.hard)
                                    }
                                    vbox(spacing = 4.0) {
                                        alignmentProperty().set(Pos.TOP_CENTER)
                                        label("Iн намаг, А") {
                                        }.addClass(Styles.hard)
                                        textFieldI1 = textfield {
                                            maxWidth = 100.0
                                        }.addClass(Styles.hard)
                                    }
                                    vbox(spacing = 4.0) {
                                        alignmentProperty().set(Pos.TOP_CENTER)
                                        label("Pн акт, Вт") {
                                        }.addClass(Styles.hard)
                                        textFieldP1 = textfield {
                                            maxWidth = 100.0
                                        }.addClass(Styles.hard)
                                    }
                                }
                                textAreaExperiment = textarea {
                                    prefHeight = 326.0
                                }.addClass(Styles.hard)
                            }
                        }
                        hBoxResult = hbox(spacing = 32.0) {
                            vbox(spacing = 16.0) {
                                alignmentProperty().set(Pos.TOP_CENTER)
                                label("Общие данные:") {
                                }.addClass(Styles.hard)
                                textAreaTotalInfo = textarea {
                                    prefHeight = 440.0
                                }.addClass(Styles.hard)
                            }
                            vbox(spacing = 16.0) {
                                alignmentProperty().set(Pos.TOP_CENTER)
                                label("Результаты испытания:") {
                                }.addClass(Styles.hard)
                                textAreaResults = textarea {
                                    prefHeight = 440.0
                                }.addClass(Styles.hard)
                            }
                        }
                    }
                }
            }
        }
    }.addClass(Styles.baseColorFoo, medium)

    fun TextField.callKeyBoard() {
        onTouchPressed = EventHandler {
            Desktop.getDesktop()
                .open(Paths.get("C:/Program Files/Common Files/Microsoft Shared/ink/TabTip.exe").toFile())
            requestFocus()
        }
    }


    fun fillFields(currentItem: ExperimentObjectsType) {
        textFieldOutside.text = currentItem.outsideD
        textFieldInside.text = currentItem.insideD
        textFieldIronLength.text = currentItem.ironLength
        textFieldBackHeight.text = currentItem.backHeight
        if (currentItem.material == "Сталь") {
            comboBoxMaterial.selectionModel.select(1)
        } else if (currentItem.material == "Алюминий") {
            comboBoxMaterial.selectionModel.select(0)
        }
        if (currentItem.insulation == "Лак") {
            comboBoxInsulation.selectionModel.select(0)
        } else if (currentItem.insulation == "Оксидирование") {
            comboBoxInsulation.selectionModel.select(1)
        }
    }

    private fun clearFields() {
        comboboxTypeObject.selectionModel.select(0)
        textFieldFacNumber.text = ""
        textFieldOutside.text = ""
        textFieldInside.text = ""
        textFieldIronLength.text = ""
        textFieldBackHeight.text = ""
        comboBoxMaterial.selectionModel.select(0)
        comboBoxInsulation.selectionModel.select(0)
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
