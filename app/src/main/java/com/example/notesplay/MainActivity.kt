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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import android.widget.ArrayAdapter
import android.content.ContentValues
import android.os.Environment
import java.text.SimpleDateFormat
import java.util.*
import android.widget.LinearLayout
import androidx.core.view.isVisible

private const val CAMERA_PERMISSION_REQUEST = 100
private const val CAMERA_REQUEST_CODE = 101
private const val GALLERY_PERMISSION_REQUEST = 200
private const val GALLERY_REQUEST_CODE = 201
private const val TAG = "NotesPlay"

class MainActivity : AppCompatActivity() {

    private lateinit var noteEditText: EditText
    private lateinit var saveNoteButton: Button
    private lateinit var createFolderButtonMain: Button
    private lateinit var viewFoldersButton: Button
    private lateinit var captureNoteButton: Button
    private lateinit var importNoteButton: Button
    private lateinit var searchView: SearchView
    private lateinit var noteTitleEditTextMain: EditText
    private var selectedFolder: String? = null
    private var searchResultsDialog: AlertDialog? = null
    private lateinit var searchResultItems: MutableList<Pair<String, String>>
    private lateinit var searchResultItemsAdapter: ArrayAdapter<String>
    private var imageBitmap: Bitmap? = null
    private var imageFileName: String? = null
    private var extractedText: String? = null
    private lateinit var renameEditText: EditText
    private lateinit var renameDialog: AlertDialog
    private var currentImageToRename: String? = null


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
        noteTitleEditTextMain = findViewById(R.id.noteTitleEditTextMain)

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
            val noteTitle = noteTitleEditTextMain.text.toString().trim()

            if (noteText.isNotEmpty()) {
                if (noteTitle.isNotEmpty()) {
                    showFolderSelectionDialog(noteText, noteTitle)
                } else {
                    Toast.makeText(this, "Please provide a note title.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter some text in the note.", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        searchResultItems = mutableListOf()

        searchResultItemsAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            mutableListOf()
        )

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

        setupRenameDialog()
    }

    private fun performSearch(query: String) {
        val rootDir = filesDir
        searchResultItems.clear()
        searchInFolder(rootDir, query)

        if (searchResultItems.isNotEmpty()) {
            searchResultItemsAdapter.clear()
            val resultStrings = searchResultItems.map { "${it.first} (in ${it.second})" }
            searchResultItemsAdapter.addAll(resultStrings)
            searchResultItemsAdapter.notifyDataSetChanged()

            if (searchResultsDialog == null || !searchResultsDialog!!.isShowing) {
                showSearchResultsDialog()
            }
        } else {
            searchResultsDialog?.dismiss()
            searchResultsDialog = null
            Toast.makeText(this, "No notes found matching '$query'.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun searchInFolder(folder: File, query: String) {

        if (folder.name.contains(query, ignoreCase = true)) {
            searchResultItems.add(Pair("📁 Folder: ${folder.name}", folder.parentFile?.name ?: "Root"))
        }

        folder.listFiles()?.forEach { file ->
            if (file.isFile) {
                val fileName = file.name
                val fileContent = readFileContent(file)

                if (fileName.contains(query, ignoreCase = true) || fileContent.contains(query, ignoreCase = true)) {

                    searchResultItems.add(Pair("📄 $fileName (in ${folder.name})", folder.name))
                }
            } else if (file.isDirectory) {
                searchInFolder(file, query)
            }
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

    private fun showSearchResultsDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Search Results")
        builder.setAdapter(searchResultItemsAdapter) { dialog, which ->
            val selectedResult = searchResultItems[which]
            val fileName = selectedResult.first
            val folderName = selectedResult.second
            openNote(fileName, folderName)

        }
        builder.setPositiveButton("Close") { dialog, _ ->
            dialog.dismiss()
            searchResultsDialog = null
        }
        searchResultsDialog = builder.create()
        searchResultsDialog?.show()
    }

    private fun openNote(fileName: String, folderName: String) {
        if (fileName.startsWith("📄")) {
            val actualFileName = fileName.substringAfter("📄 ").substringBefore(" (in ")
            val fileExtension = if (actualFileName.endsWith(".txt")) ".txt" else if (actualFileName.endsWith(".jpg")) ".jpg" else ""
            if (fileExtension == ".txt") {
                val intent = Intent(this, ViewNoteActivity::class.java)
                intent.putExtra("FOLDER_NAME", folderName)
                intent.putExtra("NOTE_FILE_NAME", actualFileName)
                startActivity(intent)
            } else if (fileExtension == ".jpg") {
                val intent = Intent(this, ViewImageNoteActivity::class.java)
                intent.putExtra("FOLDER_NAME", folderName)
                intent.putExtra("NOTE_FILE_NAME", actualFileName)
                startActivity(intent)
            }

        } else if (fileName.startsWith("📁")) {
            val folderNameOnly = fileName.substringAfter("📁 Folder: ")
            val intent = Intent(this, FolderListActivity::class.java)
            intent.putExtra("FOLDER_NAME", folderNameOnly)
            startActivity(intent)
        }
    }


    private fun showFolderSelectionDialog(noteText: String, noteTitle: String) {
        val filesDir = filesDir
        val directories = filesDir.listFiles { file -> file.isDirectory }
        val folderNames = directories?.map { it.name }?.toTypedArray() ?: arrayOf("default_folder")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Folder to Save Note")
            .setItems(folderNames) { dialog, which ->
                val selectedFolder = folderNames[which]
                saveNoteToFile(noteText, selectedFolder, "$noteTitle.txt")
                dialog.dismiss()
                Toast.makeText(
                    this,
                    "Note saved to folder '$selectedFolder' with title '$noteTitle'!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    private fun showImageFolderSelectionDialog() {
        val filesDir = filesDir
        val directories = filesDir.listFiles { file -> file.isDirectory }
        val folderNames = directories?.map { it.name }?.toTypedArray() ?: arrayOf("default_folder")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Folder to Save Image")
            .setItems(folderNames) { dialog, which ->
                selectedFolder = folderNames[which]
                val imageToSave = imageBitmap
                val fileNameToSave = imageFileName
                val textToSave = extractedText
                if (imageToSave != null && fileNameToSave != null) {
                    currentImageToRename = fileNameToSave
                    if (textToSave != null) {
                        renameEditText.setText(fileNameToSave.replace(".jpg", ""))
                    } else {
                        renameEditText.setText(fileNameToSave.replace(".jpg", ""))
                    }
                    renameDialog.show()
                } else {
                    Toast.makeText(this, "Error: No image to save.", Toast.LENGTH_SHORT).show()
                }

                dialog.dismiss()

            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
                imageBitmap = null
                imageFileName = null
                extractedText = null
                selectedFolder = null
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera()
            } else {
                Toast.makeText(this, "Camera permission denied.", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == GALLERY_PERMISSION_REQUEST) {
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
            imageBitmap = data?.extras?.get("data") as? Bitmap
            imageFileName = generateUniqueFileName(".jpg")
            imageBitmap?.let {
                recognizeText(it, imageFileName!!)
                showImageFolderSelectionDialog()
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
                    imageBitmap = bitmap
                    imageFileName = generateUniqueFileName(".jpg")
                    bitmap?.let {
                        recognizeTextFromFile(uri)
                        showImageFolderSelectionDialog()
                    } ?: run {
                        Toast.makeText(
                            this,
                            "Error decoding image from gallery.",
                            Toast.LENGTH_SHORT
                        ).show()
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

    private fun recognizeText(bitmap: Bitmap, imageName: String) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        val image = InputImage.fromBitmap(bitmap, 0)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                extractedText = visionText.text
                if (extractedText != null && extractedText!!.isNotEmpty()) {
                    Log.d(TAG, "Extracted Text: $extractedText")
                } else {
                    extractedText = null
                    Log.d(TAG, "No text found")
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Log.e(TAG, "Text recognition failed", e)
            }
    }

    private fun recognizeTextFromFile(imageUri: Uri) {
        try {
            val image = InputImage.fromFilePath(this, imageUri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    extractedText = visionText.text
                    if (extractedText != null && extractedText!!.isNotEmpty()) {
                        Log.d(TAG, "Extracted Text: $extractedText")
                    } else {
                        extractedText = null
                        Log.d(TAG, "No text found")
                    }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    Log.e(TAG, "Text recognition failed", e)
                }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(TAG, "Error loading image", e)
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

    private fun saveNoteToFile(noteText: String, folderName: String, fileName: String) {
        val folderPath = File(filesDir, folderName)
        if (!folderPath.exists()) {
            folderPath.mkdirs()
        }

        val file = File(folderPath, fileName)
        try {
            FileOutputStream(file).use {
                it.write(noteText.toByteArray())
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error saving note: ${e.message}", Toast.LENGTH_SHORT).show()
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
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fileOutputStream)
            fileOutputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error saving image note.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkGalleryPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            true
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

    private fun setupRenameDialog() {
        val builder = AlertDialog.Builder(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_rename, null)
        renameEditText = view.findViewById(R.id.renameEditText)
        builder.setView(view)
        builder.setTitle("Rename Note")
        builder.setPositiveButton("Save") { dialog, _ ->
            val newName = renameEditText.text.toString().trim()
            if (newName.isNotEmpty() && currentImageToRename != null) {

                val selectedFolderToUse = selectedFolder ?: "image_notes"
                val imageToSave = imageBitmap
                val textToSave = extractedText
                val newImageName = "$newName.jpg"
                val newTextName = "$newName.txt"
                if (imageToSave != null) {
                    saveImageToFile(imageToSave, selectedFolderToUse, newImageName)
                }
                if (textToSave != null) {
                    saveTextToFile(textToSave, selectedFolderToUse, newTextName)
                }

                Toast.makeText(this, "Note renamed to $newName", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                imageBitmap = null
                imageFileName = null
                extractedText = null
                currentImageToRename = null
                selectedFolder = null 
            } else {
                Toast.makeText(this, "Please enter a valid name.", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
            imageBitmap = null
            imageFileName = null
            extractedText = null
            currentImageToRename = null
            selectedFolder = null
        }
        renameDialog = builder.create()
    }
}
