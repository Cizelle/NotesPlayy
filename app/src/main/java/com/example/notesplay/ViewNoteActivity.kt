package com.example.notesplay

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
    private var currentFolderName: String? = null
    private var currentNoteFileName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_note)

        noteEditText = findViewById(R.id.noteEditText)
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
        currentFolderName?.let { folderName ->
            currentNoteFileName?.let { fileName ->
                val file = File(filesDir, File(folderName, fileName).path)
                try {
                    val fileInputStream = FileInputStream(file)
                    val buffer = ByteArray(fileInputStream.available())
                    fileInputStream.read(buffer)
                    val noteContent = String(buffer)
                    noteEditText.setText(noteContent)
                    fileInputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                    noteEditText.setText("Error loading note.")
                }
            }
        }
    }

    private fun saveEditedNote() {
        currentFolderName?.let { folderName ->
            currentNoteFileName?.let { fileName ->
                val file = File(filesDir, File(folderName, fileName).path)
                val editedText = noteEditText.text.toString()
                try {
                    val fileOutputStream = FileOutputStream(file)
                    fileOutputStream.write(editedText.toByteArray())
                    fileOutputStream.close()
                    Toast.makeText(this, "Changes saved!", Toast.LENGTH_SHORT).show()
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error saving changes.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}