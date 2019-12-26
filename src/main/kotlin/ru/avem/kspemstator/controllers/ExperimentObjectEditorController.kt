package ru.avem.kspemstator.controllers

import javafx.collections.ObservableList
import javafx.geometry.Pos
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import ru.avem.kspemstator.database.entities.ExperimentObject
import ru.avem.kspemstator.database.entities.ObjectsTable
import ru.avem.kspemstator.view.ExperimentObjectEditorWindow
import tornadofx.Controller
import tornadofx.controlsfx.warningNotification
import tornadofx.observable
import tornadofx.selectedItem

class ExperimentObjectEditorController : Controller() {
    private val view: ExperimentObjectEditorWindow by inject()

    private var mark: String = ""
    private var density: Double = 0.0
    private var losses: Double = 0.0
    private var intensity: Double = 0.0

    private val editorWindow: ExperimentObjectEditorWindow by inject()

    private fun areFieldsValid(): Boolean {
        if (isValuesEmpty()) {
            warningNotification(
                "Заполнение полей",
                "Заполните все поля и повторите снова.",
                Pos.BOTTOM_CENTER
            )
            return false
        }

        if (isInvalidData()) {
            warningNotification(
                "Заполнение полей",
                "Проверьте корректность заполнения полей и повторите снова.",
                Pos.BOTTOM_CENTER
            )
            return false
        }

        return true
    }

    private fun isValuesEmpty(): Boolean {
        return view.textFieldMark.text.isNullOrEmpty() ||
                view.textFieldDensity.text.isNullOrEmpty() ||
                view.textfieldLosses.text.isNullOrEmpty() ||
                view.textfieldIntensity.text.isNullOrEmpty()
    }

    private fun isInvalidData(): Boolean {
        try {
            mark = view.textFieldMark.text
            density = view.textFieldDensity.text.toDouble()
            losses = view.textfieldLosses.text.toDouble()
            intensity = view.textfieldIntensity.text.toDouble()
        } catch (e: NumberFormatException) {
            return true
        }
        return false
    }

    fun addObject(): Boolean {
        return if (areFieldsValid()) {
            transaction {

                val markDouble = ExperimentObject.find() {
                    ObjectsTable.mark eq view.textFieldMark.text
                }

                if (markDouble.empty()) {
                    ExperimentObject.new {
                        mark = view.textFieldMark.text
                        density = view.textFieldDensity.text
                        losses = view.textfieldLosses.text
                        intensity = view.textfieldIntensity.text
                    }
                }
            }
            clearViews()
            true
        } else {
            false
        }
    }


    fun deleteObject() {
        val item = editorWindow.tableViewObjects.selectedItem
        if (item != null) {
            transaction {
                ObjectsTable.deleteWhere { ObjectsTable.mark eq item.mark }
            }
        }
    }

    fun refreshObjectsTable() {
        editorWindow.tableViewObjects.items = getObjects()
    }

    fun getObjects(): ObservableList<ExperimentObject> {
        return transaction {
            ExperimentObject.all().toList().observable()
        }
    }

    private fun clearViews() {
        view.textFieldMark.clear()
        view.textFieldDensity.clear()
        view.textfieldLosses.clear()
        view.textfieldIntensity.clear()
    }
}
