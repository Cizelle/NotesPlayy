package com.example.notesplay

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class ViewNoteActivity : AppCompatActivity() {

    private lateinit var noteEditText: EditText
    private lateinit var saveEditedNoteButton: Button
    private lateinit var noteTitleEditText: EditText
    private var currentFolderName: String? = null
    private var currentNoteFileName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_note)

        noteEditText = findViewById(R.id.noteEditText) //edit
        noteTitleEditText = findViewById(R.id.noteTitleEditText)
        saveEditedNoteButton = findViewById(R.id.saveEditedNoteButton)

        currentFolderName = intent.getStringExtra("FOLDER_NAME")
        currentNoteFileName = intent.getStringExtra("NOTE_FILE_NAME")

        loadNoteContent()
        supportActionBar?.title = currentNoteFileName

        saveEditedNoteButton.setOnClickListener {
            saveEditedNote()
        }
    }

    private fun loadNoteContent() {
        currentNoteFileName?.let { fileName ->
            currentFolderName?.let { folderName ->
                val file = File(filesDir, File(folderName, fileName).path)
                if (file.exists()) {
                    try {
                        val text = FileInputStream(file).bufferedReader().use { it.readText() }
                        noteEditText.setText(text)

                        val title = fileName.removeSuffix(".txt").removeSuffix(".jpg")
                        noteTitleEditText.setText(title)

                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(this, "Error loading note.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun saveEditedNote() {
        val noteContent = noteEditText.text.toString()
        var newNoteTitle = noteTitleEditText.text.toString().trim()

        if (newNoteTitle.isEmpty()) {
            newNoteTitle = "Untitled Note ${System.currentTimeMillis()}"
        }

        val newFileNameWithExtension = if (currentNoteFileName?.endsWith(".jpg") == true) {
            "$newNoteTitle.jpg"
        } else {
            "$newNoteTitle.txt"
        }

        currentFolderName?.let { folderName ->
            val oldFile = currentNoteFileName?.let { File(filesDir, File(folderName, it).path) }
            val newFile = File(filesDir, File(folderName, newFileNameWithExtension).path)

            if (oldFile == null || oldFile.name == newFileNameWithExtension || !newFile.exists()) {
                try {
                    FileOutputStream(newFile).use { it.write(noteContent.toByteArray()) }

                    if (currentNoteFileName?.endsWith(".jpg") == true && currentNoteFileName?.replace(".jpg", ".txt") != newFileNameWithExtension.replace(".jpg", ".txt")) {
                        val oldTextFile = currentNoteFileName?.replace(".jpg", ".txt")?.let { File(filesDir, File(folderName, it).path) }
                        val newTextFile = newFileNameWithExtension.replace(".jpg", ".txt").let { File(filesDir, File(folderName, it).path) }
                        oldTextFile?.renameTo(newTextFile)
                    }

                    currentNoteFileName = newFileNameWithExtension
                    Toast.makeText(this, "Note saved as '$newFileNameWithExtension'.", Toast.LENGTH_SHORT).show()
                    val resultIntent = Intent()
                    setResult(RESULT_OK, resultIntent)
                    finish()
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error saving note.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "A note with the title '$newNoteTitle' already exists.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}