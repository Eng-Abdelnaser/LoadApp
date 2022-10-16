package com.udacity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)
        if(intent?.extras != null){
            fileNameTV.text = "File Name :- ${intent.getStringExtra("fileName")}"
            statusTV.text = "Status  :- ${intent.getStringExtra("status")}"
        }

        ok_btn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

}
