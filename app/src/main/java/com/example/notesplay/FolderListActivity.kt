package com.example.notesplay

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import androidx.appcompat.app.AlertDialog


class FolderListActivity : AppCompatActivity() {

    private lateinit var folderRecyclerView: RecyclerView
    private lateinit var folderAdapter: FolderAdapter
    private val folders = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_folder_list)

        folderRecyclerView = findViewById(R.id.folderRecyclerView)
        folderRecyclerView.layoutManager = LinearLayoutManager(this)

        loadFolders()
        folderAdapter = FolderAdapter(folders) { folderName ->
            val intent = Intent(this, NoteListActivity::class.java)
            intent.putExtra("FOLDER_NAME", folderName)
            startActivity(intent)
        }
        folderRecyclerView.adapter = folderAdapter
    }

    private fun loadFolders() {
        val filesDir = filesDir
        val directories = filesDir.listFiles { file -> file.isDirectory }

        directories?.forEach {
            folders.add(it.name)
        }
        folders.sort()
    }

    fun showDeleteFolderConfirmationDialog(folderName: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete Folder")
            .setMessage("Are you sure you want to delete '$folderName' and all its contents?")
            .setPositiveButton("Delete") { dialog, _ ->
                deleteFolder(folderName)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    private fun deleteFolder(folderName: String) {
        val directoryToDelete = File(filesDir, folderName)
        if (directoryToDelete.exists() && directoryToDelete.isDirectory) {
            directoryToDelete.listFiles()?.forEach { file ->
                file.delete()
            }
            directoryToDelete.delete()
            folders.remove(folderName)
            folderAdapter.notifyDataSetChanged()
            Toast.makeText(this, "Folder '$folderName' deleted.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Error deleting folder '$folderName'.", Toast.LENGTH_SHORT).show()
        }
    }

    fun showRenameFolderDialog(oldFolderName: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Rename Folder")

        val input = EditText(this)
        input.setText(oldFolderName)
        builder.setView(input)

        builder.setPositiveButton("Rename") { dialog, _ ->
            val newFolderName = input.text.toString().trim()
            if (newFolderName.isNotEmpty() && newFolderName != oldFolderName) {
                renameFolder(oldFolderName, newFolderName)
            } else if (newFolderName == oldFolderName) {
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

    private fun renameFolder(oldFolderName: String?, newFolderName: String) {
        val oldDirectory = File(filesDir, oldFolderName)
        val newDirectory = File(filesDir, newFolderName)

        if (oldDirectory.exists() && oldDirectory.isDirectory && !newDirectory.exists()) {
            if (oldDirectory.renameTo(newDirectory)) {
                val index = folders.indexOf(oldFolderName)
                if (index != -1) {
                    folders[index] = newFolderName
                    folderAdapter.notifyItemChanged(index)
                }
                Toast.makeText(this, "Folder '$oldFolderName' renamed to '$newFolderName'.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Error renaming folder.", Toast.LENGTH_SHORT).show()
            }
        } else if (newDirectory.exists()) {
            Toast.makeText(this, "A folder with that name already exists.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Error: Original folder not found.", Toast.LENGTH_SHORT).show()
        }
    }
}