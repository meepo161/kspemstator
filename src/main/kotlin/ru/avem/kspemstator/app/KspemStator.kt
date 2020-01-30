package ru.avem.kspemstator.app

import javafx.scene.image.Image
import javafx.scene.input.KeyCombination
import javafx.stage.Stage
import javafx.stage.StageStyle
import ru.avem.kspemstator.communication.CommunicationModel
import ru.avem.kspemstator.communication.ModbusConnection
import ru.avem.kspemstator.database.validateDB
import ru.avem.kspemstator.view.MainView
import ru.avem.kspemstator.view.Styles
import tornadofx.App
import tornadofx.FX

class KspemStator : App(MainView::class, Styles::class) {

    override fun init() {
        validateDB()
    }

    override fun start(stage: Stage) {
        stage.isFullScreen = false
        stage.isResizable = false
        stage.fullScreenExitKeyCombination = KeyCombination.NO_MATCH
        stage.initStyle(StageStyle.TRANSPARENT)
        super.start(stage)
        FX.primaryStage.icons += Image("logo.png")
        initializeSingletons()
    }

    private fun initializeSingletons() {
        ModbusConnection
        CommunicationModel
    }

    override fun stop() {
        ModbusConnection.isAppRunning = false
    }
}