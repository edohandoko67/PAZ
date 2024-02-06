package com.programmerakhirzaman.paz

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.programmerakhirzaman.paz.activity.WebActivity
import com.programmerakhirzaman.paz.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.call.setOnClickListener {
            sendToCall()
        }

        binding.website.setOnClickListener {
            startActivity(Intent(this, WebActivity::class.java))
        }

    }

    private fun sendToCall() {
        val telepon = "089523526520"
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$telepon")
        startActivity(intent)
    }
}