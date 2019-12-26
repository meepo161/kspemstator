package ru.avem.kspemstator.controllers

import javafx.scene.control.Alert
import org.jetbrains.exposed.sql.transactions.transaction
import ru.avem.kspemstator.database.entities.*
import ru.avem.kspemstator.view.MainView
import tornadofx.Controller
import tornadofx.observable

class MainViewController : Controller() {
    val view: MainView by inject()


    fun isValuesEmpty(): Boolean {
        return  view.comboboxTypeObject.selectionModel.isEmpty ||
                view.textFieldFacNumber.text.isNullOrEmpty() ||
                view.textFieldOutside.text.isNullOrEmpty() ||
                view.textFieldInside.text.isNullOrEmpty() ||
                view.textFieldBackHeight.text.isNullOrEmpty() ||
                view.textFieldIronLength.text.isNullOrEmpty() ||
                view.comboBoxMaterial.selectionModel.selectedItem.isNullOrEmpty() ||
                view.comboBoxMark.selectionModel.isEmpty ||
                view.comboBoxInsulation.selectionModel.selectedItem.isNullOrEmpty()
    }

    fun showAboutUs() {
        val alert = Alert(Alert.AlertType.INFORMATION)
        alert.title = "Версия ПО"
        alert.headerText = "Версия: 0.0.1b"
        alert.contentText = "Дата: 05.12.2019"
        alert.showAndWait()
    }

    fun refreshUsers() {
        view.comboboxUserSelector.items = transaction {
            User.find {
                Users.login notLike "admin"
            }.toList().observable()
        }
    }

    fun refreshObjects() {
        view.comboBoxMark.items = transaction {
            ExperimentObject.find {
                ObjectsTable.mark.isNotNull()
            }.toList().observable()
        }
    }

    fun refreshObjectsTypes() {
        view.comboboxTypeObject.items = transaction {
            ExperimentObjectsType.find {
                ObjectsTypes.objectType.isNotNull()
            }.toList().observable()
        }
    }
}
