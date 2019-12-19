package ru.avem.kspemstator.app

import javafx.scene.image.Image
import javafx.stage.Stage
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
        stage.isResizable = false
        super.start(stage)
        FX.primaryStage.icons += Image("logo.png")
    }

}