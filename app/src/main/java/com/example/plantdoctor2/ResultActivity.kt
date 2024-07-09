package com.example.plantdoctor2

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ResultActivity : AppCompatActivity()  {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_result)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val textView: TextView = findViewById(R.id.id_detected_results)

        if (intent?.action == Intent.ACTION_VIEW && intent.type == "text/plain") {
            textView.text = intent.getStringExtra(Intent.EXTRA_TEXT)
            // textView.text = stringResource(R.string.show_results)
        }
    }
}