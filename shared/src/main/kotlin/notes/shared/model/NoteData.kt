package notes.shared.model

import javafx.beans.InvalidationListener
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableObjectValue
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.control.ToolBar
import notes.shared.Constants
import notes.shared.SysInfo
import org.jsoup.Jsoup
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class TextChange {
    INSERT,
    DELETE,
    ITALICIZE,
    UNITALICIZE,
    UNDERLINE,
    UNUNDERLINE,
    BOLD,
    UNBOLD,
    LIST,
    UNLIST,
    COLOR,
    UNCOLOR
}

class NoteData(val id: Int, var title: String): ObservableObjectValue<NoteData?> {
    private var changeListeners = mutableListOf<ChangeListener<in NoteData>?>()
    private var invalidationListeners = mutableListOf<InvalidationListener?>()

    var body = "<html dir=\"ltr\"><head></head><body contenteditable=\"true\"></body></html>\n"
    var dateCreated = LocalDateTime.now()
    var dateEdited = LocalDateTime.now()
    var isActive = false
    var isDisplay = true

    var undoStack = ArrayList<Pair<TextChange, String>>()
    var redoStack = ArrayList<Pair<TextChange, String>>()

    constructor( id: Int, title: String, body: String, dateCreated: String, dateEdited: String ) : this( id, title ) {
        this.body = body
        this.dateCreated = LocalDateTime.parse( dateCreated )
        this.dateEdited = LocalDateTime.parse( dateEdited )
    }

    override fun addListener(listener: ChangeListener<in NoteData?>?) { changeListeners.add(listener) }

    override fun addListener(listener: InvalidationListener?) { invalidationListeners.add(listener) }

    override fun removeListener(listener: ChangeListener<in NoteData?>?) { changeListeners.remove(listener) }

    override fun removeListener(listener: InvalidationListener?) { invalidationListeners.remove(listener) }

    override fun getValue(): NoteData { return this }

    override fun get(): NoteData { return this }

    fun setNoteTitle(newTitle: String) {
        if (newTitle == "") {
            title = "New Note"

        } else {
            title = newTitle
        }
        dateEdited = LocalDateTime.now()


        invalidationListeners.forEach { it?.invalidated(this) }
        changeListeners.forEach { it?.changed(this, this.value, value) }
    }
    fun changeNoteTitle(newTitle: String) {
        title = newTitle

        invalidationListeners.forEach { it?.invalidated(this) }
        changeListeners.forEach { it?.changed(this, this.value, value) }
    }

    fun getNoteTitle(): String { return title }

    fun setNoteBody(newBody: String) {
        this.body = newBody
        dateEdited = LocalDateTime.now()
        setDateHTMLEditor()
        invalidationListeners.forEach { it?.invalidated(this) }
        changeListeners.forEach { it?.changed(this, this.value, value) }
    }

    fun changeNoteBody(newBody: String) {
        this.body = newBody

        invalidationListeners.forEach { it?.invalidated(this) }
        changeListeners.forEach { it?.changed(this, this.value, value) }
    }

    fun setDateHTMLEditor() {
        val toolBar2: ToolBar = Constants.notesArea.lookup(".bottom-toolbar") as ToolBar

        toolBar2.items.forEach { e -> println(e) }

        val date = toolBar2.lookup(".label")
        if(date is Label) {
            if (this.isActive) {
                date.text = this.getDateEdited()
                println("html editor date updated")
            }
        }


    }

    fun setTitleHTMLEditor() {
        val toolBar2: ToolBar = Constants.notesArea.lookup(".bottom-toolbar") as ToolBar
        val title = toolBar2.lookup(".text-field")
        if(title is TextField) {
            if (this.isActive) {
                title.text = this.getNoteTitle()
                println("html editor title updated")
            }
        }
    }

    fun clearTitleAndDateHTMLEditor() {
        val toolBar2: ToolBar = Constants.notesArea.lookup(".bottom-toolbar") as ToolBar

        // must set title first, then date due to the listener on title (that updates date when title changes)
        val title = toolBar2.lookup(".text-field")
        if(title is TextField) {
            title.text = ""
            println("html editor title cleared")
        }
        val date = toolBar2.lookup(".label")
        if(date is Label) {
            date.text = ""
            println("html editor date cleared")
        }

    }

    fun changeBodyBackgroundColor(backgroundColor: String) : String {
        val beginningStyleIndex = this.body.indexOf("<body style=")
        val endingStyleIndex = this.body.indexOf("contenteditable=")

        if (beginningStyleIndex == -1) { // no style set yet
            var bodyTagIndex = this.body.indexOf("<body")
            var beginningSubstring = this.body.substring(0, bodyTagIndex + 5)
            var endingSubstring = this.body.substring(bodyTagIndex + 5)
            return "$beginningSubstring style='background-color: $backgroundColor;'$endingSubstring"
        }
        else { // has style already
            var beginningSubstring = this.body.substring(0, beginningStyleIndex + 5)
            var endingSubstring = this.body.substring(endingStyleIndex)
            return "$beginningSubstring 'background-color: $backgroundColor;'$endingSubstring"
        }
    }

    fun getHTML(): String { return body }

    fun getText(): String { return Jsoup.parse( getHTML() ).text() }

    fun getPreview(): String { return getText().take(100) }

    fun getDateCreated(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return dateCreated.format(formatter)
    }

    fun getDateEdited(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return dateEdited.format(formatter)
    }

    fun setActive() {
        isActive = true

        invalidationListeners.forEach { it?.invalidated(this) }
        changeListeners.forEach { it?.changed(this, this.value, value) }
    }

    fun setInactive() {
        isActive = false

        invalidationListeners.forEach { it?.invalidated(this) }
        changeListeners.forEach { it?.changed(this, this.value, value) }
    }

    fun doDisplay() {
        isDisplay = true
    }

    fun notDisplay() {
        isDisplay = false
    }

    fun undo(): String? {
        if (undoStack.isNotEmpty()) {
            val action = undoStack.last()
            undoStack.removeLast()

            when (action.first) {
                TextChange.INSERT -> {
                    redoStack.add(Pair(TextChange.DELETE, getHTML()))
                }

                TextChange.DELETE -> {
                    redoStack.add(Pair(TextChange.INSERT, getHTML()))
                }

                TextChange.ITALICIZE -> {
                    redoStack.add(Pair(TextChange.UNITALICIZE, getHTML()))
                }

                TextChange.UNITALICIZE -> {
                    redoStack.add(Pair(TextChange.ITALICIZE, getHTML()))
                }

                TextChange.BOLD -> {
                    redoStack.add(Pair(TextChange.UNBOLD, getHTML()))
                }

                TextChange.UNBOLD -> {
                    redoStack.add(Pair(TextChange.BOLD, getHTML()))
                }

                TextChange.UNDERLINE -> {
                    redoStack.add(Pair(TextChange.UNUNDERLINE, getHTML()))
                }

                TextChange.UNUNDERLINE -> {
                    redoStack.add(Pair(TextChange.UNDERLINE, getHTML()))
                }

                TextChange.LIST -> {
                    redoStack.add(Pair(TextChange.UNLIST, getHTML()))
                }

                TextChange.UNLIST -> {
                    redoStack.add(Pair(TextChange.LIST, getHTML()))
                }

                TextChange.COLOR -> {
                    redoStack.add(Pair(TextChange.UNCOLOR, getHTML()))
                }

                TextChange.UNCOLOR -> {
                    redoStack.add(Pair(TextChange.COLOR, getHTML()))
                }
            }
            setNoteBody(action.second)
            print("UNDO")
            return action.second
        }
        return null
    }

    fun addToUndoStack(type: TextChange) {
        undoStack.add(Pair(type, getHTML()))
    }

    fun redo(): String? {
        if (redoStack.isNotEmpty()) {
            val action = redoStack.last()
            redoStack.removeLast()

            when (action.first) {
                TextChange.INSERT -> {
                    undoStack.add(Pair(TextChange.DELETE, getHTML()))
                }

                TextChange.DELETE -> {
                    undoStack.add(Pair(TextChange.INSERT, getHTML()))
                }

                TextChange.ITALICIZE -> {
                    undoStack.add(Pair(TextChange.UNITALICIZE, getHTML()))
                }

                TextChange.UNITALICIZE -> {
                    undoStack.add(Pair(TextChange.ITALICIZE, getHTML()))
                }

                TextChange.BOLD -> {
                    undoStack.add(Pair(TextChange.UNBOLD, getHTML()))
                }

                TextChange.UNBOLD -> {
                    undoStack.add(Pair(TextChange.BOLD, getHTML()))
                }

                TextChange.UNDERLINE -> {
                    undoStack.add(Pair(TextChange.UNUNDERLINE, getHTML()))
                }

                TextChange.UNUNDERLINE -> {
                    undoStack.add(Pair(TextChange.UNDERLINE, getHTML()))
                }

                TextChange.LIST -> {
                    undoStack.add(Pair(TextChange.UNLIST, getHTML()))
                }

                TextChange.UNLIST -> {
                    undoStack.add(Pair(TextChange.LIST, getHTML()))
                }

                TextChange.COLOR -> {
                    undoStack.add(Pair(TextChange.UNCOLOR, getHTML()))
                }

                TextChange.UNCOLOR -> {
                    undoStack.add(Pair(TextChange.COLOR, getHTML()))
                }
            }
            setNoteBody(action.second)
            print("REDO")
            return action.second
        }
        return null
    }

    fun emptyRedo() {
        redoStack.clear()
    }
}