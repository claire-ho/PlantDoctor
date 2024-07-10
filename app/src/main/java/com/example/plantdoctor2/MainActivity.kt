package com.example.plantdoctor2

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    val TAG = "PlantDoctor-Main"
    val image_file_from_camera: String = ""
    val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result: ActivityResult ->
        val result_obj = "Photo capture succeeded: ${result}"
        // Toast.makeText(baseContext, result_obj, Toast.LENGTH_LONG).show()
        Log.d(TAG, result_obj)
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            val image_file = intent?.getStringExtra("IMAGE_FILE") ?: ""
            val msg = "Image filename: " + image_file
            Log.d(TAG, msg)

        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val detectButton: ImageButton = findViewById<ImageButton>(R.id.tree_button)
        detectButton.setOnClickListener {v ->
            val context = v.context
            // val message = getMessage()

            val intent = Intent(context, CameraActivity::class.java)
            startForResult.launch(intent)
        }
    }
}

@Composable
fun getMessage(): String {
    var title = stringResource(R.string.detected_results)
    return title
}