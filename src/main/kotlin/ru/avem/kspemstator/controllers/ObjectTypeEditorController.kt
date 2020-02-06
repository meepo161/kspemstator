package ru.avem.kspemstator.controllers

import javafx.collections.ObservableList
import javafx.geometry.Pos
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import ru.avem.kspemstator.database.entities.ExperimentObjectsType
import ru.avem.kspemstator.database.entities.ObjectsTypes
import ru.avem.kspemstator.utils.Toast
import ru.avem.kspemstator.view.ObjectTypeEditorWindow
import tornadofx.Controller
import tornadofx.controlsfx.warningNotification
import tornadofx.observable
import tornadofx.selectedItem

class ObjectTypeEditorController : Controller() {

    private val window: ObjectTypeEditorWindow by inject()
    private val mainView: MainViewController by inject()

    private fun areFieldsValid(): Boolean {
        if (isValuesEmpty()) {
            warningNotification(
                "Заполнение полей",
                "Заполните все поля и повторите снова.",
                Pos.BOTTOM_CENTER
            )
            return false
        }

        if (!isValuesDouble()) {
            warningNotification(
                "Заполнение полей",
                "Проверьте корректность заполнения полей и повторите снова.",
                Pos.BOTTOM_CENTER
            )
            return false
        }

        return true
    }

    fun isValuesEmpty(): Boolean {
        return window.textfieldOutside.text.isNullOrEmpty() ||
                window.textfieldInside.text.isNullOrEmpty() ||
                window.textfieldPower.text.isNullOrEmpty() ||
                window.textfieldFrequency.text.isNullOrEmpty() ||
                window.textfieldBackHeight.text.isNullOrEmpty() ||
                window.textfieldIronLength.text.isNullOrEmpty() ||
                window.comboBoxMaterial.selectionModel.selectedItem.isNullOrEmpty() ||
                window.comboBoxInsulation.selectionModel.selectedItem.isNullOrEmpty() ||
                window.comboBoxMark.selectionModel.selectedItem == null
    }

    fun isValuesDouble(): Boolean {
        return try {
            window.textfieldOutside.text.toDouble()
            window.textfieldPower.text.toDouble()
            window.textfieldFrequency.text.toDouble()
            window.textfieldInside.text.toDouble()
            window.textfieldBackHeight.text.toDouble()
            window.textfieldIronLength.text.toDouble()
            true
        } catch (e: Exception) {
            Toast.makeText("Неверно заполнены поля").show(Toast.ToastType.ERROR)
            false
        }
    }

    fun addObject(): Boolean {
        return if (areFieldsValid()) {
            transaction {
                val newObjectType = ExperimentObjectsType.find() {
                    ObjectsTypes.objectType eq window.textfieldObjectType.text
                }

                if (newObjectType.empty()) {
                    ExperimentObjectsType.new {
                        objectType = window.textfieldObjectType.text
                        power = window.textfieldPower.text
                        frequency = window.textfieldFrequency.text
                        insideD = window.textfieldInside.text
                        outsideD = window.textfieldOutside.text
                        ironLength = window.textfieldIronLength.text
                        backHeight = window.textfieldBackHeight.text
                        material = window.comboBoxMaterial.selectionModel.selectedItem
                        insulation = window.comboBoxInsulation.selectionModel.selectedItem
                        mark = window.comboBoxMark.selectionModel.selectedItem.toString()
                    }
                }
            }
            clearViews()
            mainView.refreshObjectsTypes()
            true
        } else {
            false
        }
    }


    fun deleteObject() {
        val item = window.tableViewObjects.selectedItem
        if (item != null) {
            transaction {
                ObjectsTypes.deleteWhere { ObjectsTypes.objectType eq item.objectType }
            }
        }
        mainView.refreshObjectsTypes()
    }

    fun refreshMarksTypes() {
        window.tableViewObjects.items = getObjects()
    }

    fun getObjects(): ObservableList<ExperimentObjectsType> {
        return transaction {
            ExperimentObjectsType.all().toList().observable()
        }
    }

    private fun clearViews() {
        window.textfieldObjectType.clear()
        window.textfieldPower.clear()
        window.textfieldFrequency.clear()
        window.textfieldInside.clear()
        window.textfieldOutside.clear()
        window.textfieldIronLength.clear()
        window.textfieldBackHeight.clear()
    }
}
