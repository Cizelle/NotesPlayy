package com.example.notesplay

import android.content.Intent
import android.os.Bundle
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
            intent.putExtra("FOLDER_NAME", folderName) // Pass the folder name
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
                file.delete() // Delete all files within the folder
            }
            directoryToDelete.delete() // Delete the folder itself
            folders.remove(folderName)
            folderAdapter.notifyDataSetChanged()
            Toast.makeText(this, "Folder '$folderName' deleted.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Error deleting folder '$folderName'.", Toast.LENGTH_SHORT).show()
        }
    }
}