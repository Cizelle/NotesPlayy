package com.example.notesplay

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader

private const val CAMERA_PERMISSION_REQUEST = 100
private const val CAMERA_REQUEST_CODE = 101
private const val GALLERY_PERMISSION_REQUEST = 200
private const val GALLERY_REQUEST_CODE = 201

class MainActivity : AppCompatActivity() {

    private lateinit var noteEditText: EditText
    private lateinit var saveNoteButton: Button
    private lateinit var createFolderButtonMain: Button
    private lateinit var viewFoldersButton: Button
    private lateinit var captureNoteButton: Button
    private lateinit var importNoteButton: Button
    private lateinit var searchView: SearchView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        noteEditText = findViewById(R.id.noteEditText)
        saveNoteButton = findViewById(R.id.saveNoteButton)
        createFolderButtonMain = findViewById(R.id.createFolderButtonMain)
        viewFoldersButton = findViewById(R.id.viewFoldersButton)
        captureNoteButton = findViewById(R.id.captureNoteButton)
        importNoteButton = findViewById(R.id.importNoteButton)
        searchView = findViewById(R.id.searchView)

        createFolderButtonMain.setOnClickListener {
            val intent = Intent(this, CreateFolderActivity::class.java)
            startActivity(intent)
        }

        viewFoldersButton.setOnClickListener {
            val intent = Intent(this, FolderListActivity::class.java)
            startActivity(intent)
        }

        captureNoteButton.setOnClickListener {
            if (checkCameraPermission()) {
                launchCamera()
            } else {
                requestCameraPermission()
            }
        }

        importNoteButton.setOnClickListener {
            if (checkGalleryPermission()) {
                launchGallery()
            } else {
                requestGalleryPermission()
            }
        }

        saveNoteButton.setOnClickListener {
            val noteText = noteEditText.text.toString()
            if (noteText.isNotEmpty()) {
                showFolderSelectionDialog(noteText)
            } else {
                Toast.makeText(this, "Please enter some text.", Toast.LENGTH_SHORT).show()
            }
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { performSearch(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { performSearch(it) }
                return true
            }
        })
    }

    private fun performSearch(query: String) {
        val searchResults = mutableListOf<Pair<String, String>>()
        val rootDir = filesDir

        fun searchInFolder(folder: File) {
            folder.listFiles()?.forEach { file ->
                if (file.isFile) {
                    val fileName = file.name
                    val fileContent = readFileContent(file)
                    if (fileContent.contains(query, ignoreCase = true)) {
                        searchResults.add(Pair(fileName, folder.name))
                    }
                } else if (file.isDirectory) {
                    searchInFolder(file)
                }
            }
        }

        searchInFolder(rootDir)

        if (searchResults.isNotEmpty()) {
            showSearchResultsDialog(searchResults)
        } else {
            Toast.makeText(this, "No notes found matching '$query'.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun readFileContent(file: File): String {
        var content = ""
        try {
            val fis = FileInputStream(file)
            val isr = InputStreamReader(fis)
            val br = BufferedReader(isr)
            val sb = StringBuilder()
            var line = br.readLine()
            while (line != null) {
                sb.append(line).append("\n")
                line = br.readLine()
            }
            content = sb.toString()
            br.close()
            isr.close()
            fis.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return content
    }

    private fun showSearchResultsDialog(results: List<Pair<String, String>>) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Search Results")
        val resultItems = results.map { "${it.first} (in ${it.second})" }.toTypedArray()
        builder.setItems(resultItems) { dialog, which ->
            val selectedResult = results[which]
            val fileName = selectedResult.first
            val folderName = selectedResult.second
            // Open the selected note
            if (fileName.endsWith(".txt")) {
                val intent = Intent(this, ViewNoteActivity::class.java)
                intent.putExtra("FOLDER_NAME", folderName)
                intent.putExtra("NOTE_FILE_NAME", fileName)
                startActivity(intent)
            } else if (fileName.endsWith(".jpg")) {
                val intent = Intent(this, ViewImageNoteActivity::class.java)
                intent.putExtra("FOLDER_NAME", folderName)
                intent.putExtra("NOTE_FILE_NAME", fileName)
                startActivity(intent)
            }
        }
        builder.setPositiveButton("Close") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }








    private fun showFolderSelectionDialog(noteText: String) {
        val filesDir = filesDir
        val directories = filesDir.listFiles { file -> file.isDirectory }
        val folderNames = directories?.map { it.name }?.toTypedArray() ?: arrayOf("default_folder")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Save Note To Folder")
            .setItems(folderNames) { dialog, which ->
                val selectedFolder = folderNames[which]
                saveNoteToFile(noteText, selectedFolder, generateUniqueFileName(".txt"))
                noteEditText.text.clear()
                Toast.makeText(this, "Note saved to $selectedFolder!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST
        )
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera()
            } else {
                Toast.makeText(this, "Camera permission denied.", Toast.LENGTH_SHORT).show()
            }
        }
        if (requestCode == GALLERY_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchGallery()
            } else {
                Toast.makeText(this, "Gallery permission denied.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun launchCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as? Bitmap
            imageBitmap?.let {
                recognizeText(it) // Perform OCR
                saveImageToFile(it, "image_notes", generateUniqueFileName(".jpg"))
                Toast.makeText(this, "Image note saved!", Toast.LENGTH_SHORT).show()
            } ?: run {
                Toast.makeText(this, "Error capturing image.", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                try {
                    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val source = ImageDecoder.createSource(contentResolver, uri)
                        ImageDecoder.decodeBitmap(source)
                    } else {
                        MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    }
                    bitmap?.let {
                        recognizeText(it) // Perform OCR
                        saveImageToFile(it, "image_notes", generateUniqueFileName(".jpg"))
                        Toast.makeText(this, "Image from gallery saved!", Toast.LENGTH_SHORT).show()
                    } ?: run {
                        Toast.makeText(this, "Error decoding image from gallery.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error loading image from gallery.", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                Toast.makeText(this, "Error selecting image from gallery.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun recognizeText(bitmap: Bitmap) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) // Using Latin options

        val image = InputImage.fromBitmap(bitmap, 0)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val extractedText = visionText.text
                if (extractedText.isNotEmpty()) {
                    val imageName = generateUniqueFileName(".jpg")
                    val textFileName = imageName.replace(".jpg", ".txt")
                    saveTextToFile(extractedText, "image_notes", textFileName)
                    Toast.makeText(this, "Text extracted (including handwriting?) and saved!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "No text found in the image.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(this, "Error during text recognition.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveTextToFile(text: String, folderName: String, fileName: String) {
        val directory = File(filesDir, folderName)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = File(directory, fileName)
        try {
            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(text.toByteArray())
            fileOutputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error saving extracted text.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun generateUniqueFileName(extension: String): String {
        return "note_${System.currentTimeMillis()}${extension}"
    }

    private fun saveNoteToFile(note: String, folderName: String, fileName: String) {
        val directory = File(filesDir, folderName)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = File(directory, fileName)
        try {
            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(note.toByteArray())
            fileOutputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error saving text note.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveImageToFile(bitmap: Bitmap, folderName: String, fileName: String) {
        val directory = File(filesDir, folderName)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = File(directory, fileName)
        try {
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fileOutputStream) // Compress image
            fileOutputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error saving image note.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkGalleryPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            true // No runtime permission needed for media on Android 13+
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestGalleryPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                GALLERY_PERMISSION_REQUEST
            )
        } else {
            launchGallery()
        }
    }

    private fun launchGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun saveImageFromUri(uri: Uri, folderName: String, fileName: String) {
        val directory = File(filesDir, folderName)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = File(directory, fileName)
        try {
            val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }

            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fileOutputStream)
            fileOutputStream.close()
            Toast.makeText(this, "Image from gallery saved!", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error saving image from gallery.", Toast.LENGTH_SHORT).show()
        }
    }

}