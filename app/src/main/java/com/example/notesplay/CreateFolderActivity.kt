package com.example.notesplay

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class CreateFolderActivity : AppCompatActivity() {

    private lateinit var folderNameEditText: EditText
    private lateinit var createFolderButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_folder)

        folderNameEditText = findViewById(R.id.folderNameEditText)
        createFolderButton = findViewById(R.id.createFolderButton)

        createFolderButton.setOnClickListener {
            val folderName = folderNameEditText.text.toString().trim()
            if (folderName.isNotEmpty()) {
                val directory = File(filesDir, folderName)
                if (!directory.exists()) {
                    if (directory.mkdirs()) {
                        Toast.makeText(this, "Folder '$folderName' created!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Failed to create folder.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Folder '$folderName' already exists.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter a folder name.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}