package notes.view

import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.scene.control.Label
import javafx.scene.input.MouseButton
import javafx.scene.layout.Background
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import notes.shared.model.Model
import notes.shared.model.NoteData


class NoteList(val noteModel: Model): VBox() {
    inner class NotePreview(noteData: NoteData): VBox() {
        val title = Label( noteData.title )
        val date = Label( noteData.getDateEdited() ).apply {
            // TODO set the min width here to be the size of the text contained by the label so it is not truncated
            minWidth = this.layoutBounds.width

        }
        val preview = Label( noteData.getPreview() ).apply {
            HBox.setHgrow(this, Priority.NEVER)
        }
        fun refresh(noteData: NoteData) {
            title.text = noteData.title
            date.text = noteData.getDateEdited()
            preview.text = noteData.getPreview()
            background = if (noteData.isActive) Background.fill(Color.AQUA)
            else Background.fill(Color.TRANSPARENT)
        }
        init {
            val dateAndPreview = HBox(date, preview).apply {
                HBox.setMargin(date, Insets(0.0, 5.0, 0.0, 0.0))
            }
            children.addAll( title, dateAndPreview )
            prefHeight = 50.0

            this.setOnMouseClicked{e ->
                if (e.getButton() == MouseButton.PRIMARY) {
                    noteModel.setActiveNote(noteData)
                    println("Note named ${noteData.title} set as active note with body ${noteData.getHTML()}")
                } else if (e.getButton() == MouseButton.SECONDARY) {

                }
            }

            noteData.addListener { _, _, newValue ->
                println("detected a change in note ${newValue?.title}")
                if (newValue != null) refresh(newValue)
            }
        }
    }
    fun refreshList(noteList: ObservableList<NoteData>) {
        children.clear()
        for (noteData in noteList) {
            if(noteData.isDisplay) {
                children.add(NotePreview(noteData))
            }
        }
    }
    init {
        minWidth = 200.0
        maxWidth = 500.0

        refreshList( noteModel.notes )

        noteModel.notes.addListener(ListChangeListener {
            println("detected a change in note list")
            refreshList( noteModel.notes )
        })
    }
}