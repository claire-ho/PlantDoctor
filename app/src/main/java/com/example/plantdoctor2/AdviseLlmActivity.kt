package com.example.plantdoctor2

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.ViewModel
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.launch

class AdviseLlmActivity: AppCompatActivity() {
    val TAG = "PlantDoctor:AdviseLlmActivity"
    val generativeModel = GenerativeModel(
        // The Gemini 1.5 models are versatile and work with both text-only and multimodal prompts
        modelName = "gemini-1.5-flash",
        // Access your API key as a Build Configuration variable (see "Set up your API key" above)
        apiKey = BuildConfig.apiKey
    )

    @SuppressLint("WrongThread")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_llm_advise)
        if (Build.VERSION.SDK_INT > 9) {
            val policy = ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        var desease:String? = null
        if (intent?.action == Intent.ACTION_VIEW && intent.type == "text/plain") {
            desease = intent.getStringExtra(Intent.EXTRA_TEXT)
        }
        if (desease == null || desease.isEmpty()) {
            finish()
        }


        val titleView: TextView = findViewById(R.id.id_title)
        val adviseView: TextView = findViewById(R.id.id_advise)
        val okButton: Button = findViewById<Button>(R.id.id_ok_button)
        titleView.text = desease + " - Waiting for suggestions from Gemini ..."

        val prompt = "Please answer in Traditional Chines: Tell me the treatments for PassionFruit with $desease decease."
        lifecycleScope.launch {
            val response: GenerateContentResponse = generateContent(prompt)
            adviseView.text = response.text
            // adviseView.text = " This is a layout test! \n We need to display the title ...."
        }
        okButton.setOnClickListener { finish() }
    }

    private final suspend fun generateContent(
        prompt: String
    ): GenerateContentResponse {
        val response = generativeModel.generateContent(prompt)
        response.text?.let { Log.d(TAG, it) }
        return response
    }
}
