package com.programmerakhirzaman.paz

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
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

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            val isGranted = it.values.reduce { acc, next -> acc && next }
            if (!isGranted) {
                val builder = AlertDialog.Builder(this)
                    .setTitle("Judul")
                    .setMessage("Pesan")
                    .setPositiveButton("Pengaturan") { dialog, _ ->
                        dialog.dismiss()
                        finish()
                        val i = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        i.data = Uri.parse("package:com.programmerakhirzaman.paz")
                        startActivity(i)
                    }
                    .setCancelable(false)
                val dialog = builder.create()
                dialog.show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        checkPermissions()

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
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
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
        val url = "https://api.whatsapp.com/send?phone=$whatappUser&text=${
            URLEncoder.encode(
                message,
                "UTF-8"
            )
        }"
        try {
            val packageManager = applicationContext.packageManager
            packageManager.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        } catch (e: PackageManager.NameNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    private fun sendToCall() {
        val telepon = "+6289523526520"
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$telepon")
        startActivity(intent)
    }

    private fun checkPermissions() {
        val checkPermissionsResult = requiredPermissions.all {
            ContextCompat.checkSelfPermission(
                applicationContext,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }

        if (!checkPermissionsResult) {
            requestPermissionsLauncher.launch(requiredPermissions)
        }
    }

    private val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.READ_MEDIA_IMAGES
        )
    } else {
        arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

}