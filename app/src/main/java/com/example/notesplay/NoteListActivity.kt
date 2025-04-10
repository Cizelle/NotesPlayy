package com.example.notesplay

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import androidx.appcompat.app.AlertDialog


class NoteListActivity : AppCompatActivity() {

    private lateinit var noteRecyclerView: RecyclerView
    private lateinit var noteAdapter: NoteAdapter
    private val notes = mutableListOf<String>()
    var currentFolderName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_list)

        noteRecyclerView = findViewById(R.id.noteRecyclerView)
        noteRecyclerView.layoutManager = LinearLayoutManager(this)

        currentFolderName = intent.getStringExtra("FOLDER_NAME")
        loadNotes()

        noteAdapter = NoteAdapter(notes, currentFolderName) { noteFileName ->
        }
        noteRecyclerView.adapter = noteAdapter
        supportActionBar?.title = currentFolderName
    }

    private fun loadNotes() {
        currentFolderName?.let { folderName ->
            val directory = File(filesDir, folderName)
            if (directory.exists() && directory.isDirectory) {
                val noteFiles = directory.listFiles { file -> file.isFile && (file.name.endsWith(".txt") || file.name.endsWith(".jpg")) }
                noteFiles?.forEach {
                    notes.add(it.name)
                }
                notes.sort()
            }
        }
    }

    fun showDeleteNoteConfirmationDialog(noteFileName: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete Note")
            .setMessage("Are you sure you want to delete '$noteFileName'?")
            .setPositiveButton("Delete") { dialog, _ ->
                currentFolderName?.let { folderName ->
                    deleteNote(folderName, noteFileName)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    private fun deleteNote(folderName: String, noteFileName: String) {
        val fileToDelete = File(filesDir, File(folderName, noteFileName).path)
        if (fileToDelete.exists() && fileToDelete.isFile) {
            fileToDelete.delete()
            notes.remove(noteFileName)
            noteAdapter.notifyDataSetChanged()
            Toast.makeText(this, "Note '$noteFileName' deleted.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Error deleting note '$noteFileName'.", Toast.LENGTH_SHORT).show()
        }
    }
}