package com.example.plantdoctor2

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputEditText
import okhttp3.FormBody
import okhttp3.Headers.Companion.toHeaders
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class AdviseChatGptActivity : AppCompatActivity() {

    val TAG:String = "PlantDoctor-AdviseChatGPTActivity"
    // val url = "https://api.openai.com/v1/completions"  // Legacy, for gpt-3.5-turbo-instruct, babbage-002, davinci-002
    val url = "https://api.openai.com/v1/chat/completions"
    var desease:String = ""

    // creating variables on below line.
    lateinit var responseTV: TextView
    lateinit var questionTV: TextView
    lateinit var queryEdt: TextInputEditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chapgpt_advise)
        // initializing variables on below line.
        responseTV = findViewById(R.id.idTVResponse)
        questionTV = findViewById(R.id.idTVQuestion)
        queryEdt = findViewById(R.id.idEdtQuery)

        if (intent?.action == Intent.ACTION_VIEW && intent.type == "text/plain") {
            desease = intent.getStringExtra(Intent.EXTRA_TEXT).toString()
        }
        if (desease == null || desease.isEmpty()) {
            finish()
        }
        var initPrompt = "How to cure $desease of my PassionFruit trees?  Please answer in Traditional Chines."
        getResponse(initPrompt)
        // adding editor action listener for edit text on below line.
        queryEdt.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                // setting response tv on below line.
                responseTV.text = "Waiting for suggestions from ChapGPT ...\""
                // validating text
                if (queryEdt.text.toString().length > 0) {
                    // calling get response to get the response.
                    getResponse(queryEdt.text.toString())
                } else {
                    Toast.makeText(this, "Please enter your query..", Toast.LENGTH_SHORT).show()
                }
                return@OnEditorActionListener true
            }
            false
        })
    }

    private fun getResponse(query: String) {
        // setting text on for question on below line.
        questionTV.text = query
        queryEdt.setText("")
        var prompt = questionTV.text
        val client = OkHttpClient()

        var headers = mapOf("Content-Type" to "application/json",
            "Authorization" to "Bearer " + BuildConfig.chatGptKey)
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()?: MultipartBody.FORM
        val body = """
            {
            "model": "gpt-3.5-turbo",
            "messages": [{"role": "user", "content": "$query"}],
            "temperature": 0.2,
            "n": 1,
            "max_tokens": 300
            }
        """.trimIndent()

    val request = Request.Builder()
            .url(url)
            .post(body.toRequestBody(mediaType))
            .headers(headers.toHeaders())
            .build()

        Log.d(TAG, "Sending OpenAI post request with body:" + body)
        val response = client.newCall(request).execute()
        val responseBody = response.body!!.string()

        //Response
        Log.d(TAG, "Response Body: $responseBody")
        var results = JSONObject(responseBody)
        val responseMsg: String =
            results.getJSONArray("choices").getJSONObject(0)
                .getString("message")
        responseTV.text = JSONObject(responseMsg).getString("content")
    }

    private fun getPrompt(query:String): List<MutableMap<String, String>> {
        var messages = mutableMapOf("role" to "user")
        messages["content"] = query
        var promptList = listOf(messages)

        return promptList
    }
}