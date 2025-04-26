package com.example.notesplay

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class ViewImageNoteActivity : AppCompatActivity() {

    private lateinit var imageNoteView: ImageView
    private lateinit var extractedEditText: EditText
    private lateinit var saveExtractedTextButton: Button
    private var currentFolderName: String? = null
    private var currentImageFileName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_image_note)

        imageNoteView = findViewById(R.id.imageNoteView)
        extractedEditText = findViewById(R.id.extractedEditText)
        saveExtractedTextButton = findViewById(R.id.saveExtractedTextButton)

        currentFolderName = intent.getStringExtra("FOLDER_NAME")
        currentImageFileName = intent.getStringExtra("NOTE_FILE_NAME")

        loadImage()
        loadExtractedText()
        supportActionBar?.title = currentImageFileName

        saveExtractedTextButton.setOnClickListener {
            saveEditedText()
        }
    }

    private fun loadImage() {
        currentFolderName?.let { folderName ->
            currentImageFileName?.let { fileName ->
                val imageFile = File(filesDir, File(folderName, fileName).path)
                if (imageFile.exists()) {
                    val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                    imageNoteView.setImageBitmap(bitmap)
                } else {
                    imageNoteView.setImageResource(android.R.drawable.ic_menu_report_image)
                }
            }
        }
    }

    private fun loadExtractedText() {
        currentFolderName?.let { folderName ->
            currentImageFileName?.let { imageFileName ->
                val textFileName = imageFileName.replace(".jpg", ".txt")
                val textFile = File(filesDir, File(folderName, textFileName).path)
                if (textFile.exists()) {
                    try {
                        val fileInputStream = FileInputStream(textFile)
                        val buffer = ByteArray(fileInputStream.available())
                        fileInputStream.read(buffer)
                        val extractedText = String(buffer)
                        extractedEditText.setText(extractedText)
                        fileInputStream.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                        extractedEditText.setText("Error loading extracted text.")
                    }
                }
            }
        }
    }

    private fun saveEditedText() {
        currentFolderName?.let { folderName ->
            currentImageFileName?.let { imageFileName ->
                val textFileName = imageFileName.replace(".jpg", ".txt")
                val textFile = File(filesDir, File(folderName, textFileName).path)
                val editedText = extractedEditText.text.toString()
                try {
                    val fileOutputStream = FileOutputStream(textFile)
                    fileOutputStream.write(editedText.toByteArray())
                    fileOutputStream.close()
                    Toast.makeText(this, "Text changes saved!", Toast.LENGTH_SHORT).show()
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error saving text changes.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}