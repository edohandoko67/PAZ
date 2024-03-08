package com.programmerakhirzaman.paz

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.programmerakhirzaman.paz.activity.WebActivity
import com.programmerakhirzaman.paz.activity.WebView
import com.programmerakhirzaman.paz.databinding.ActivityMainBinding
import com.programmerakhirzaman.paz.location.DefaultLocationClient
import com.programmerakhirzaman.paz.location.LocationClient
import com.programmerakhirzaman.paz.model.modalLocation
import java.net.URLEncoder


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val whatappUser = "+6289523526520"
    private lateinit var database: DatabaseReference

//    val wakeLock: PowerManager.WakeLock =
//        (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
//            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag").apply {
//                acquire()
//            }
//        }

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

        Intent(this, LocationService::class.java).also {
            startService(it)
        }

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

        database = FirebaseDatabase.getInstance().reference



    }


//    fun writeLocation(userId: String,latitude: Double, longitude: Double){
//        val modal = modalLocation(latitude, longitude)
//
//        database.child("users").child(userId).setValue(modal)
//    }


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
            android.Manifest.permission.READ_MEDIA_IMAGES,
            android.Manifest.permission.POST_NOTIFICATIONS
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

    class LocationService : Service() {
        private lateinit var locationClient: LocationClient
        private lateinit var fusedLocation: FusedLocationProviderClient
        private lateinit var database: DatabaseReference
        override fun onBind(p0: Intent?): IBinder? {
            return null
        }

        override fun onCreate() {
            super.onCreate()
            locationClient = DefaultLocationClient(
                applicationContext,
                LocationServices.getFusedLocationProviderClient(applicationContext)
            )
            fusedLocation = LocationServices.getFusedLocationProviderClient(applicationContext)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }

            database = FirebaseDatabase.getInstance().reference

            fusedLocation.lastLocation.addOnSuccessListener {it
                if (it != null) {
                    val latitude = it.latitude.toString()
                    val longitude = it.longitude.toString()
                    Log.d("LocationService", "Latitude: $latitude, Longitude: $longitude")

//                    val userLatitude = latitude
//                    val userLongitude = longitude

                    val idRoute = database.push().key
                    val STD = modalLocation(idRoute.toString(),latitude.toDouble(), longitude.toDouble())
                    database.child(idRoute.toString()).setValue(STD)
                    database.child(idRoute.toString()).setValue(STD).addOnCompleteListener {

                    }
                } else {
                    Log.e("LocationService", "Last known location is null")
                }
            }
                .addOnFailureListener {
                    Log.e("LocationService", "Failed to get last location: ${it.message}")
                }
        }
    }
}