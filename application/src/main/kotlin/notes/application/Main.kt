package notes.application

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.layout.BorderPane
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import notes.shared.SysInfo
import notes.model.Model

class Main : Application() {
    override fun start(stage: Stage) {
        stage.scene = Scene(
            BorderPane(Label(Model.testMessage)),
            250.0,
            150.0)
        stage.isResizable = true
        stage.minWidth = 600.0
        stage.minHeight = 400.0
        stage.title = "Notes"
        stage.show()
    }
}