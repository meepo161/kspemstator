package ru.avem.kspemstator.view

import javafx.geometry.Pos
import javafx.scene.control.TextField
import ru.avem.kspemstator.controllers.AddExperimentObjectController
import ru.avem.kspemstator.utils.Toast
import tornadofx.*

class AddExperimentObjectWindow : View("Добавить материал и его характеристики") {
    var textFieldMaterial: TextField by singleAssign()
    var textFieldMark: TextField by singleAssign()
    var textFieldInsulation: TextField by singleAssign()
    var textFieldDensity: TextField by singleAssign()
    var textfieldLosses: TextField by singleAssign()
    var textfieldIntensity: TextField by singleAssign()

    private val controller: AddExperimentObjectController by inject()


    override val root = anchorpane {
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

                label("Материал станины:")
                textFieldMaterial = textfield {
                    prefWidth = 200.0
                }
            }

            hbox(spacing = 16.0) {
                alignmentProperty().set(Pos.CENTER_RIGHT)

                label("Марка стали:")
                textFieldMark = textfield {
                    prefWidth = 200.0
                }
            }

            hbox(spacing = 16.0) {
                alignmentProperty().set(Pos.CENTER_RIGHT)

                label("Тип изоляции:")
                textFieldInsulation = textfield {
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
                        controller.addObject()
                        Toast.makeText("Новый объект добавлен.").show(Toast.ToastType.INFORMATION)
                    }
                }
            }
        }.addClass(Styles.medium)
    }
}
