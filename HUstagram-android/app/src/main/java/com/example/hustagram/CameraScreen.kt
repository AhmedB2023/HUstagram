package com.example.hustagram.ui

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MultipartBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody

@Composable
fun CameraScreen() {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var comment by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // 📸 Launcher to take photo
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (!success) {
            imageUri = null // Cancelled
        }
    }

    // Function to upload image to Flask server
    fun uploadImage(imageUri: Uri, comment: String) {
        // You will need to implement a method that sends a POST request with the image and comment
        val client = OkHttpClient()
        val mediaType = "image/jpeg".toMediaTypeOrNull()
        val imageFile = File(imageUri.path ?: "")
        val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("image", imageFile.name, imageFile.asRequestBody(mediaType))
            .addFormDataPart("comment", comment)
            .build()

        val request = Request.Builder()
            .url("http://127.0.0.1:5000/upload") // Flask server URL
            .post(requestBody)
            .build()

        scope.launch {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    // Handle success
                } else {
                    // Handle failure
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(onClick = {
            val uri = createImageUri(context)
            imageUri = uri
            takePictureLauncher.launch(uri)
        }) {
            Text("Take Picture")
        }

        imageUri?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = "Captured Image",
                modifier = Modifier.fillMaxWidth().height(300.dp)
            )
        }

        // Comment TextField
        OutlinedTextField(
            value = comment,
            onValueChange = { comment = it },
            label = { Text("Comment") },
            modifier = Modifier.fillMaxWidth()
        )

        // Upload Button
        Button(
            onClick = {
                if (imageUri != null && comment.isNotEmpty()) {
                    uploadImage(imageUri!!, comment)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Upload")
        }
    }
}

// 📂 Creates a file URI for the camera to write into
fun createImageUri(context: Context): Uri {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val imageFile = File(
        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
        "IMG_$timestamp.jpg"
    )
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        imageFile
    )
}

