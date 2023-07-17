package com.example.textfromimagemlkit

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.textfromimagemlkit.databinding.ActivityMainBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private var imageUri: Uri? = null

    private lateinit var textRecognizer: TextRecognizer

    private val permissions = arrayOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private val cameraPermissionResult =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionGranted ->
            val grant = permissionGranted.entries.all { it.value }
            if (grant) {
                takePictureCamera()
            } else {
                Toast.makeText(
                    this@MainActivity, "The camera permission is necessary",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        binding.takePictures.setOnClickListener {
            cameraPermissionResult.launch(permissions)
        }

        binding.recognizetext.setOnClickListener {
            if (imageUri != null) {
                recognizeImageFromText()
            }
        }
    }

    private fun recognizeImageFromText() {
        try {
            val inputImage = InputImage.fromFilePath(this, imageUri!!)
            textRecognizer.process(inputImage)
                .addOnSuccessListener { text ->
                    binding.textview.text = text.text
                }
                .addOnFailureListener {
                }
        } catch (e: Exception) {

        }
    }

    private fun takePictureCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Sample Title")
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)

        cameraActivityResultLauncher.launch(intent)
    }

    private val cameraActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                binding.imageview.setImageURI(imageUri)
            }
        }
}