package ru.avem.kspemstator.controllers

import javafx.geometry.Pos
import org.jetbrains.exposed.sql.transactions.transaction
import ru.avem.kspemstator.database.entities.ExperimentObject
import ru.avem.kspemstator.view.AddExperimentObjectWindow
import tornadofx.Controller
import tornadofx.controlsfx.warningNotification

class AddExperimentObjectController : Controller() {
    private val view: AddExperimentObjectWindow by inject()

    private var material: String = "3"
    private var mark: String = "2"
    private var density: Double = 0.0
    private var losses: Double = 0.0
    private var intensity: Double = 0.0

    fun addObject() {
        transaction {
            ExperimentObject.new {
                material = view.textFieldMaterial.text
                mark = view.textFieldMark.text
                density = view.textFieldDensity.text
                losses = view.textfieldLosses.text
                intensity = view.textfieldIntensity.text
                insulation = view.textFieldInsulation.text
            }
        }
        clearViews()
    }

    private fun clearViews() {
        view.textFieldMaterial.clear()
        view.textFieldMark.clear()
        view.textFieldDensity.clear()
        view.textfieldLosses.clear()
        view.textfieldIntensity.clear()
        view.textFieldInsulation.clear()
    }
}
