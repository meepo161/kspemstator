package ru.avem.kspemstator.controllers

import javafx.collections.ObservableList
import javafx.geometry.Pos
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import ru.avem.kspemstator.database.entities.MarksObjects
import ru.avem.kspemstator.database.entities.MarksTypes
import ru.avem.kspemstator.view.AddMarkWindow
import tornadofx.Controller
import tornadofx.controlsfx.warningNotification
import tornadofx.observable
import tornadofx.selectedItem

class AddMarkController : Controller() {

    private var mark: String = ""
    private var density: Double = 0.0
    private var losses: Double = 0.0
    private var intensity: Double = 0.0

    private val window: AddMarkWindow by inject()
    private val view: MainViewController by inject()

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
        return window.textFieldMark.text.isNullOrEmpty() ||
                window.textFieldDensity.text.isNullOrEmpty() ||
                window.textfieldLosses.text.isNullOrEmpty() ||
                window.textfieldIntensity.text.isNullOrEmpty()
    }

    private fun isInvalidData(): Boolean {
        try {
            mark = window.textFieldMark.text
            density = window.textFieldDensity.text.toDouble()
            losses = window.textfieldLosses.text.toDouble()
            intensity = window.textfieldIntensity.text.toDouble()
        } catch (e: NumberFormatException) {
            return true
        }
        return false
    }

    fun addObject(): Boolean {
        return if (areFieldsValid()) {
            transaction {

                val markDouble = MarksObjects.find() {
                    MarksTypes.mark eq window.textFieldMark.text
                }

                if (markDouble.empty()) {
                    MarksObjects.new {
                        mark = window.textFieldMark.text
                        density = window.textFieldDensity.text
                        losses = window.textfieldLosses.text
                        intensity = window.textfieldIntensity.text
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
        val item = window.tableViewObjects.selectedItem
        if (item != null) {
            transaction {
                MarksTypes.deleteWhere { MarksTypes.mark eq item.mark }
            }
        }
    }

    fun refreshMarksTypes() {
        window.tableViewObjects.items = getObjects()
    }

    fun getObjects(): ObservableList<MarksObjects> {
        return transaction {
            MarksObjects.all().toList().observable()
        }
    }

    private fun clearViews() {
        window.textFieldMark.clear()
        window.textFieldDensity.clear()
        window.textfieldLosses.clear()
        window.textfieldIntensity.clear()
    }
}
