package com.example.notesplay

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.FileInputStream
import java.io.IOException

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
        notes.clear()
        currentFolderName?.let { folderName ->
            val directory = File(filesDir, folderName)
            if (directory.exists() && directory.isDirectory) {
                val noteFiles = directory.listFiles { file ->
                    file.isFile && (file.name.endsWith(".txt") || file.name.endsWith(".jpg"))
                }
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
                    noteAdapter.notifyDataSetChanged()
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
        val directories = filesDir.listFiles { file -> file.isDirectory }
        val folderNames = directories?.map { it.name }?.toTypedArray() ?: arrayOf("default_folder")

        AlertDialog.Builder(this)
            .setTitle("Move Note To Folder")
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
                    if (noteFileName.endsWith(".jpg")) {
                        val sourceTextFile = File(filesDir, File(sourceFolder, noteFileName.replace(".jpg", ".txt")).path)
                        val destinationTextFile = File(filesDir, File(destinationFolder, noteFileName.replace(".jpg", ".txt")).path)
                        if (sourceTextFile.exists()) {
                            sourceTextFile.renameTo(destinationTextFile)
                        }
                    }
                    loadNotes()
                    noteAdapter.notifyDataSetChanged()
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

    fun getNoteContent(noteFileName: String?): String? {
        currentFolderName?.let { folderName ->
            val noteFile = File(filesDir, File(folderName, noteFileName).path)
            if (noteFile.exists()) {
                return try {
                    FileInputStream(noteFile).bufferedReader().use { it.readText() }
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error reading note content.", Toast.LENGTH_SHORT).show()
                    null
                }
            }
        }
        return null
    }

    fun startQuizGeneration(noteContent: String) {
        val sentences = noteContent.split(". ", "! ", "? ")
        val quizItems = mutableListOf<QuizItem>()
        val allKeywords = mutableSetOf<String>()

        for (sentence in sentences) {
            val words = sentence.split(" ")
            for (word in words) {
                if (word.isNotBlank() && word[0].isUpperCase()) {
                    allKeywords.add(word.trim().replace(Regex("[.,?!]$"), ""))
                }
            }
        }
        val keywordList = allKeywords.toList()

        for (sentence in sentences) {
            val words = sentence.split(" ")
            for (i in words.indices) {
                if (words[i].isNotBlank() && words[i][0].isUpperCase()) {
                    val correctAnswer = words[i].trim().replace(Regex("[.,?!]$"), "")
                    val question = sentence.replaceFirst(correctAnswer, "_____")

                    if (question != sentence) {
                        val distractors = keywordList.filter { it != correctAnswer }.shuffled().take(3)
                        val optionsList = mutableListOf(correctAnswer)
                        optionsList.addAll(distractors)
                        optionsList.shuffle()

                        val optionsString = optionsList.joinToString(", ")

                        quizItems.add(QuizItem(question, correctAnswer, optionsString))
                        break
                    }
                }
            }
        }

        if (quizItems.isNotEmpty()) {
            val intent = Intent(this, QuizActivity::class.java)
            intent.putExtra("QUIZ_QUESTIONS", quizItems as java.io.Serializable)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Could not generate quiz questions.", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VIEW_NOTE_REQUEST_CODE && resultCode == RESULT_OK) {
            notes.clear()
            loadNotes()
            noteAdapter.notifyDataSetChanged()
        }
    }

    companion object {
        const val VIEW_NOTE_REQUEST_CODE: Int = 123
    }
}