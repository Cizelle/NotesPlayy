package com.example.notesplay

import android.os.Bundle
import android.widget.EditText
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

    fun showRenameNoteDialog(oldFileName: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Rename Note")

        val input = EditText(this)
        input.setText(oldFileName)
        builder.setView(input)

        builder.setPositiveButton("Rename") { dialog, _ ->
            val newFileName = input.text.toString().trim()
            if (newFileName.isNotEmpty() && newFileName != oldFileName) {
                renameNoteFile(oldFileName, newFileName)
            } else if (newFileName == oldFileName) {
                Toast.makeText(this, "New name is the same as the old name.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "New name cannot be empty.", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    private fun renameNoteFile(oldFileName: String?, newFileName: String) {
        currentFolderName?.let { folderName ->
            val oldFile = File(filesDir, File(folderName, oldFileName).path)
            val newFileBaseName = newFileName.replace(Regex("\\.(txt|jpg)$"), "")
            val newFileNameWithExtension = if (oldFileName?.endsWith(".txt") == true) {
                "$newFileBaseName.txt"
            } else {
                "$newFileBaseName.jpg"
            }
            val newFile = File(filesDir, File(folderName, newFileNameWithExtension).path)

            if (oldFile.exists() && !newFile.exists()) {
                if (oldFile.renameTo(newFile)) {
                    if (oldFileName?.endsWith(".jpg") == true) {
                        val oldTextFile = File(filesDir, File(folderName, oldFileName.replace(".jpg", ".txt")).path)
                        val newTextFile = File(filesDir, File(folderName, newFileNameWithExtension.replace(".jpg", ".txt")).path)
                        if (oldTextFile.exists()) {
                            oldTextFile.renameTo(newTextFile)
                        }
                    }

                    loadNotes()
                    Toast.makeText(this, "Note renamed to '$newFileNameWithExtension'.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error renaming note.", Toast.LENGTH_SHORT).show()
                }
            } else if (newFile.exists()) {
                Toast.makeText(this, "A note with that name already exists.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Error: Original note not found.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun showFolderSelectionDialogForMove(noteFileName: String?) {
        val filesDir = filesDir
        val directories = filesDir.listFiles { file -> file.isDirectory }
        val folderNames = directories?.map { it.name }?.toTypedArray() ?: arrayOf("default_folder")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Move Note To Folder")
            .setItems(folderNames) { dialog, which ->
                val destinationFolder = folderNames[which]
                if (currentFolderName != destinationFolder && noteFileName != null) {
                    moveNoteFile(noteFileName, destinationFolder)
                } else if (currentFolderName == destinationFolder) {
                    Toast.makeText(this, "Note is already in this folder.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error selecting folder or note.", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    private fun moveNoteFile(noteFileName: String, destinationFolder: String) {
        currentFolderName?.let { sourceFolder ->
            val sourceFile = File(filesDir, File(sourceFolder, noteFileName).path)
            val destinationFile = File(filesDir, File(destinationFolder, noteFileName).path)

            if (sourceFile.exists() && !destinationFile.exists()) {
                if (sourceFile.renameTo(destinationFile)) {
                    // Move the associated text file if it's an image note
                    if (noteFileName.endsWith(".jpg")) {
                        val sourceTextFile = File(filesDir, File(sourceFolder, noteFileName.replace(".jpg", ".txt")).path)
                        val destinationTextFile = File(filesDir, File(destinationFolder, noteFileName.replace(".jpg", ".txt")).path)
                        if (sourceTextFile.exists()) {
                            sourceTextFile.renameTo(destinationTextFile)
                        }
                    }

                    loadNotes() // Reload the list to reflect the move
                    Toast.makeText(this, "Note '$noteFileName' moved to '$destinationFolder'.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error moving note.", Toast.LENGTH_SHORT).show()
                }
            } else if (destinationFile.exists()) {
                Toast.makeText(this, "A note with that name already exists in the destination folder.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Error: Source note not found.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}