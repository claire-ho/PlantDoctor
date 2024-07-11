package com.example.plantdoctor2

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL


class DetectionActivity : AppCompatActivity() {
    val TAG = "PlantDoctor-DetectionActivity"

    data class DetectedResults(var detectedDesease: String? = null,
                               var scores: String? = null,
                               var errMessage: String? = null)

    var alertDialog: AlertDialog? = null
    @SuppressLint("WrongThread")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_result)
        if (Build.VERSION.SDK_INT > 9) {
            val policy = ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val textView: TextView = findViewById(R.id.id_detected_results)
        val imageId: ImageView = findViewById(R.id.id_photo)
        val detectButton: Button = findViewById<Button>(R.id.id_detect_button)
        val imageBytes: ByteArrayOutputStream = ByteArrayOutputStream()
        val content_uri_builder: Uri.Builder = Uri.Builder()

        if (intent?.action == Intent.ACTION_VIEW && intent.type == "text/plain") {
            val resolver = applicationContext.contentResolver
            val content_uri_str = intent.getStringExtra(Intent.EXTRA_TEXT)
            val content_uri = Uri.parse(content_uri_str)
            content_uri_builder.path(content_uri_str)
            val photoBitmap = uriToBitmap(content_uri)?: null
            if (photoBitmap != null) {
                imageId.setImageBitmap(photoBitmap)
                photoBitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageBytes)
                textView.text = "Phone size :  " + imageBytes.size()/1000 + "K"
            }
        }

        detectButton.setOnClickListener {v ->
            val context = v.context
            textView.text = "Detecting photo size: " + imageBytes.size()/1000  + " K"
            post(imageBytes, content_uri_builder.build())
        }
    }

    private fun uriToBitmap(selectedFileUri: Uri): Bitmap? {
        try {
            val parcelFileDescriptor = contentResolver.openFileDescriptor(selectedFileUri, "r")
            val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
            val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor.close()

            val scaledImage = rotateAndResizeBitmap(image, 350, 350, 90f)
            return scaledImage
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    fun rotateAndResizeBitmap(bm: Bitmap, newWidth: Int, newHeight: Int, degrees: Float): Bitmap {
        val width = bm.width
        val height = bm.height
        val scaleWidth = (newWidth.toFloat()) / width
        val scaleHeight = (newHeight.toFloat()) / height
        // CREATE A MATRIX FOR THE MANIPULATION
        val matrix = Matrix()
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight)
        matrix.postRotate(degrees)

        // "RECREATE" THE NEW BITMAP
        val resizedBitmap = Bitmap.createBitmap(
            bm, 0, 0, width, height, matrix, false
        )
        bm.recycle()
        return resizedBitmap
    }

    private fun post(imageBytes:ByteArrayOutputStream, imageUri: Uri ) {
        Log.d(TAG, "Preparing HTTP request ....")
        val client = OkHttpClient()
        val url = URL("http://lfuh.dynu.net:8080/imageTest")
        // val url = URL("http://24.4.145.49:8080/imageTest")

        // Save bitmap to a temporary file
        val applicationContext = baseContext.applicationContext
        val mediaType = contentResolver.getType(imageUri)?.let { it.toMediaTypeOrNull() }
        val tempFileName = getTempFileName(imageUri)
        val imageFile = File(
            applicationContext.cacheDir,
            tempFileName
        )
        val outputStream = FileOutputStream(imageFile)
        imageBytes.writeTo(outputStream)

        val body: RequestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("file", imageFile.name, imageFile.asRequestBody("image/jpg".toMediaTypeOrNull()))
            .build()
/*
        val requestFile: RequestBody = imageFile.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val body4_builder: MultipartBody.Builder = MultipartBody.Builder().setType(MultipartBody.FORM)
        body4_builder
            .addFormDataPart("file", imageFile.name, requestFile) // the profile photo
        // make sure the name (ie profile_photo), matches your api, that is name of the key.
        val body4: RequestBody = body4_builder.build()


 */
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        Log.d(TAG, "Sending HTTP with ContentLength:" + body.contentLength())
        val response = client.newCall(request).execute()
        val responseBody = response.body!!.string()

        //Response
        Log.d(TAG, "Response Body: $responseBody")
        var results  = processResponse(responseBody)
        if (results.errMessage != null) {
            Toast.makeText(baseContext, responseBody, Toast.LENGTH_LONG)
                .show()
        } else {
            if (results.detectedDesease.equals("healthy!")) {
                Toast.makeText(baseContext,
                    "Detected result is \n \"HEALTHY\"!\n Scores: ${results.scores}",
                    Toast.LENGTH_LONG)
                    .show()
            } else {
                // FurtherActionDialog(baseContext)
                //   .show("Detected results: $results.detectedDesease", "Ask for advise?",)
                var desease = results.detectedDesease?.uppercase()
                createDialog("Detected results",
                    "$desease!\n\n Ask for advise?",
                    desease?:"")
                alertDialog?.show()
            }
        }
    }

    private fun getTempFileName(uri:Uri): String {
        var filename:String = "temp.jpg"

        var paths = uri.toString().split('/')
        filename = paths[paths.size-1] + ".jpg"
        Log.d(TAG, "filename: $filename" )
        return filename
    }

    private fun processResponse(detectedResponse:String): DetectedResults {
        var tokens = detectedResponse.split(",", ignoreCase = true, limit=2)
        var results : DetectedResults = DetectedResults()
        if (tokens[0].length > 20) {
            results.errMessage = tokens[0]
            return results
        }
        val DeseaseList = listOf(
            "healthy", "scab", "rust", "frog_eye_leaf_spot", "complex", "powdery_mildew",
            "scab frog_eye_leaf_spot", "scab frog_eye_leaf_spot complex",
            "rust frog_eye_leaf_spot", "rust complex",
            "frog_eye_leaf_spot complex", "powdery_mildew complex",
        )
        var desease: String = tokens[0].trim().lowercase()
        if (DeseaseList.contains(desease)) {
            results.detectedDesease = desease
            results.scores = tokens[1]
        } else {
            results.errMessage = detectedResponse
        }

        return results
    }

    fun createDialog(title:String, message: String, desease: String) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(title)
        alertDialogBuilder.setMessage(message)

        alertDialogBuilder.setNeutralButton("ChatGPT") { _: DialogInterface, _: Int ->
            // val message = getMessage()
            val intent = Intent(baseContext, AdviseChatGptActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.apply {
                action = Intent.ACTION_VIEW
                putExtra(Intent.EXTRA_TEXT, desease)
                type = "text/plain"
            }
            baseContext.startActivity(intent)
            finish()
        }
        alertDialogBuilder.setPositiveButton("Gemini") { _: DialogInterface, _: Int ->
            val intent = Intent(baseContext, AdviseLlmActivity::class.java)
            // val intent = Intent(baseContext, AdviseChatGptActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.apply {
                action = Intent.ACTION_VIEW
                putExtra(Intent.EXTRA_TEXT, desease)
                type = "text/plain"
            }
            baseContext.startActivity(intent)
            finish()
        }
        alertDialogBuilder.setNegativeButton("No") { dialogInterface: DialogInterface, _: Int ->
            finish()
        }
        alertDialog = alertDialogBuilder.create()
    }
}