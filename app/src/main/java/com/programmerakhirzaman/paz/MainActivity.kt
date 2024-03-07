package com.programmerakhirzaman.paz

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.programmerakhirzaman.paz.activity.WebActivity
import com.programmerakhirzaman.paz.activity.WebView
import com.programmerakhirzaman.paz.databinding.ActivityMainBinding
import java.net.URLEncoder

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val whatappUser = "+6289523526520"
    private val INTERNET_PERMISSION_CODE = 100
    private val REQUEST_CONTACTS_PERMISSION = 1
    private val READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 102
    private val MANAGE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 103
    private val FOREGROUND_SERVICE_PERMISSION_REQUEST_CODE = 104
    private val WAKE_LOCK_PERMISSION_REQUEST_CODE = 105
    private val ACCESS_FINE_LOCATION_PERMISSION_REQUEST_CODE = 106

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.INTERNET
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // You already have the permission
            // You can proceed with your internet related operations
        } else {
            // You don't have the permission, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.INTERNET),
                INTERNET_PERMISSION_CODE
            )
        }

        location()

        binding.call.setOnClickListener {
            sendToCall()
        }

        binding.website.setOnClickListener {
            startActivity(Intent(this, WebActivity::class.java))
        }

        binding.wa.setOnClickListener {
            sendToWa()
        }

        binding.privy.setOnClickListener {
            startActivity(Intent(this, WebView::class.java))
        }
        //notification
        fun showNotification(context: Context, title: String, message: String) {
            val channelId = "default_channel_id"
            val channelName = "Default Channel"
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
                notificationManager.createNotificationChannel(channel)
            }

            val notification = NotificationCompat.Builder(context, channelId)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build()

            notificationManager.notify(1, notification)
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

    private fun contactPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_CONTACTS),
                REQUEST_CONTACTS_PERMISSION
            )
        } else {
            // Permission has already been granted
            // You can proceed with your contact related tasks
        }
    }

    private fun location() {

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.FOREGROUND_SERVICE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.FOREGROUND_SERVICE),
                FOREGROUND_SERVICE_PERMISSION_REQUEST_CODE
            )
        }

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WAKE_LOCK
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.WAKE_LOCK),
                WAKE_LOCK_PERMISSION_REQUEST_CODE
            )
        }

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                ACCESS_FINE_LOCATION_PERMISSION_REQUEST_CODE
            )
            storagePermission()
            contactPermission()

        }
    }

    private fun storagePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE
            )
        }

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.MANAGE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.MANAGE_EXTERNAL_STORAGE),
                MANAGE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE
            )
        }
    }


}