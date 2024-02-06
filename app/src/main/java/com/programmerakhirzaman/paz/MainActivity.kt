package com.programmerakhirzaman.paz

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.programmerakhirzaman.paz.activity.WebActivity
import com.programmerakhirzaman.paz.databinding.ActivityMainBinding
import java.net.URLEncoder

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val whatappUser = "+6289523526520"
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

        binding.wa.setOnClickListener {
            sendToWa()
        }

    }

    private fun sendToWa() {
        val message = "Hai kak, berikut pesan langsung dari Website PAZ. Saya mau konsultasi kak"
        val url = "https://api.whatsapp.com/send?phone=$whatappUser&text=${URLEncoder.encode(message, "UTF-8")}"
        try {
            val packageManager = applicationContext.packageManager
            packageManager.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        } catch (e: PackageManager.NameNotFoundException){
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    private fun sendToCall() {
        val telepon = "+6289523526520"
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$telepon")
        startActivity(intent)
    }
}