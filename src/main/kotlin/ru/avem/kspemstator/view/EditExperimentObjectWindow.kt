package ru.avem.kspemstator.view

import javafx.collections.ObservableList
import javafx.scene.control.TableView
import org.jetbrains.exposed.sql.transactions.transaction
import ru.avem.kspemstator.database.entities.ExperimentObject
import tornadofx.View
import tornadofx.anchorpane
import tornadofx.observable
import tornadofx.singleAssign

internal class EditExperimentObjectWindow : View("Редактор пользователей") {

    private var tableExperimentObjects: TableView<ExperimentObject> by singleAssign()
    private val mainView: MainView by inject()

    override fun onBeforeShow() {
        modalStage!!.setOnHiding {
        }
    }

    fun refreshUsersTable() {
        tableExperimentObjects.items = getExperimentObjects()
    }

    private fun getExperimentObjects(): ObservableList<ExperimentObject> {
        return transaction {
            ExperimentObject.all().toList().observable()
        }
    }

    override val root = anchorpane {

    }
}