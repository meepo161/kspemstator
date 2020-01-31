package ru.avem.kspemstator.view

import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.TextField
import ru.avem.kspemstator.communication.CommunicationModel
import ru.avem.kspemstator.controllers.AddMarkController
import tornadofx.*
import java.awt.Desktop
import java.nio.file.Paths

class LatrWindow : View("Настройка ЛАТРа") {
    var textfieldUpOn: TextField by singleAssign()
    var textfieldUpOff: TextField by singleAssign()
    var textfieldDownOn: TextField by singleAssign()
    var textfieldDownOff: TextField by singleAssign()

    private val controller: AddMarkController by inject()
    private val mainView: MainView by inject()

    override fun onBeforeShow() {
        modalStage!!.setOnHiding {
            //            controller.refreshObjectsTable()
        }
    }

    override val root = anchorpane {

        hbox(spacing = 16.0) {
            anchorpaneConstraints {
                leftAnchor = 16.0
                rightAnchor = 16.0
                topAnchor = 16.0
                bottomAnchor = 16.0
            }
            vbox(spacing = 16.0) {
                alignmentProperty().set(Pos.CENTER)

                hbox(spacing = 16.0) {
                    alignmentProperty().set(Pos.CENTER_RIGHT)

                    label("Длительность вкл. состояния вверх:")
                    textfieldUpOn = textfield {
                        text = "0.0004"
                        prefWidth = 200.0
                        callKeyBoard()
                    }
                }

                hbox(spacing = 16.0) {
                    alignmentProperty().set(Pos.CENTER_RIGHT)

                    label("Длительность откл. состояния вверх:")
                    textfieldUpOff = textfield {
                        text = "0.002"
                        prefWidth = 200.0
                        callKeyBoard()
                    }
                }

                hbox(spacing = 16.0) {
                    alignmentProperty().set(Pos.CENTER_RIGHT)

                    label("Длительность вкл. состояния вниз")
                    textfieldDownOn = textfield {
                        text = "0.0004"
                        prefWidth = 200.0
                        callKeyBoard()
                    }
                }

                hbox(spacing = 16.0) {
                    alignmentProperty().set(Pos.CENTER_RIGHT)

                    label("Длительность откл. состояния вниз")
                    textfieldDownOff = textfield {
                        text = "0.002"
                        prefWidth = 200.0
                        callKeyBoard()
                    }
                }

                hbox(spacing = 16.0) {
                    alignmentProperty().set(Pos.CENTER)

                    button("Записать") {
                        action {
                            CommunicationModel.owenPR200Controller.setUPOn(textfieldUpOn.text.toFloat())
                            CommunicationModel.owenPR200Controller.setUPOff(textfieldUpOff.text.toFloat())
                            CommunicationModel.owenPR200Controller.setDownOn(textfieldDownOn.text.toFloat())
                            CommunicationModel.owenPR200Controller.setDownOff(textfieldDownOff.text.toFloat())
                        }
                    }
                }
            }
        }
    }.addClass(Styles.medium, Styles.baseColorFoo)

    fun TextField.callKeyBoard() {
        onTouchPressed = EventHandler {
            Desktop.getDesktop()
                .open(Paths.get("C:/Program Files/Common Files/Microsoft Shared/ink/TabTip.exe").toFile())
            requestFocus()
        }
    }
}
