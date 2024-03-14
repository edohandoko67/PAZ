package com.programmerakhirzaman.paz

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Notification
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
import android.os.PowerManager.WakeLock
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.programmerakhirzaman.paz.activity.WebActivity
import com.programmerakhirzaman.paz.activity.WebView
import com.programmerakhirzaman.paz.databinding.ActivityMainBinding
import com.programmerakhirzaman.paz.location.DefaultLocationClient
import com.programmerakhirzaman.paz.location.LocationClient
import com.programmerakhirzaman.paz.model.modalLocation
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.days


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val whatappUser = "+6289523526520"
    private lateinit var database: DatabaseReference
    private lateinit var wakeLock: PowerManager.WakeLock

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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        checkPermissions()
        Intent(this, LocationService::class.java).also {
            startService(it)
        }

        val serviceIntent = Intent(this, LocationForegroundService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)

        val powerManager: PowerManager.WakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag").apply {
                    acquire()
                }
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

    override fun onPause() {
        super.onPause()
        if (::wakeLock.isInitialized && wakeLock.isHeld) {
            wakeLock.release()
        }
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
        private val LOCATION_PERMISSION_REQUEST_CODE = 100
        private val UPDATE_INTERVAL = TimeUnit.SECONDS.toMillis(10)
        private var updateCount = 0
        private lateinit var wakeLock: PowerManager.WakeLock


        override fun onBind(intent: Intent?): IBinder? {
            return null
        }

        override fun onCreate() {
            super.onCreate()
            locationClient = DefaultLocationClient(
                applicationContext,
                LocationServices.getFusedLocationProviderClient(applicationContext)
            )
            fusedLocation = LocationServices.getFusedLocationProviderClient(applicationContext)
            database = FirebaseDatabase.getInstance().reference
            requestLocationUpdates()

            val powerManager: PowerManager.WakeLock =
                (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                    newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag").apply {
                        acquire()
                    }
                }
        }

        override fun onDestroy() {
            super.onDestroy()
            // Panggil removeLocationUpdates saat layanan dihentikan
            fusedLocation.removeLocationUpdates(locationCallback)
            if (::wakeLock.isInitialized && wakeLock.isHeld) {
                wakeLock.release()
            }
        }

        private fun requestLocationUpdates() {
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

            fusedLocation.requestLocationUpdates(
                createLocationRequest(),
                locationCallback,
                null /* Looper */
            )
        }

        private fun createLocationRequest(): LocationRequest {
            return LocationRequest.create().apply {
                interval = UPDATE_INTERVAL
                fastestInterval = UPDATE_INTERVAL / 2
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }
        }

        private val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val latitude = location.latitude
                    val longitude = location.longitude
                    Log.d("LocationService", "Latitude: $latitude, Longitude: $longitude")
                    saveLocationToDatabase(latitude, longitude)
                    updateCount++
                    if ( updateCount == 3) {
                        deleteLocationFromDatabase()
                        updateCount = 0
                    }
                }
            }
        }

        private fun saveLocationToDatabase(latitude: Double, longitude: Double) {
            val idRoute = database.push().key
            if (idRoute != null) {
                val currentDate = Date()
                val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
                val formatterDate = formatter.format(currentDate)
                val std = modalLocation(idRoute, latitude, longitude, formatterDate)
                database.child(idRoute).setValue(std)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            //Toast.makeText(applicationContext, "Success", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.e("LocationService", "Failed to save location: ${task.exception}")
                            Toast.makeText(applicationContext, "Failed to save location", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Log.e("LocationService", "Failed to generate key for location")
                Toast.makeText(applicationContext, "Failed to generate key for location", Toast.LENGTH_SHORT).show()
            }
        }

        private fun deleteLocationFromDatabase() {
            if (updateCount >= 3) {
                val query = database.orderByKey().limitToFirst(3)
                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (dataSnapshot in snapshot.children) {
                            val key = dataSnapshot.key
                            if (key != null) {
                                database.child(key).removeValue()
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("LocationService", "Failed to delete location: ${error.message}")
                    }

                })
                // Reset hitungan pembaruan
                updateCount = 0
            }
        }
    }
}

class LocationForegroundService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    companion object {
        const val CHANNEL_ID = "LocationForegroundServiceChannel"
        const val NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationRequest()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()
        requestLocationUpdates()
        return START_STICKY
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = TimeUnit.SECONDS.toMillis(10) // Update interval in milliseconds
            fastestInterval = TimeUnit.SECONDS.toMillis(5) // Fastest update interval
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private fun startForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Location Service")
            .setContentText("Running")
            .setSmallIcon(R.drawable.ic_notification)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Location Service Channel",
            NotificationManager.IMPORTANCE_HIGH
        )
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun requestLocationUpdates() {
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    Log.d("LocationForegroundService", "Location: ${location.latitude}, ${location.longitude}")
                }
            }
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}

/*
    class LocationService : Service() {
        private lateinit var locationClient: LocationClient
        private lateinit var fusedLocation: FusedLocationProviderClient
        private lateinit var database: DatabaseReference
        private val LOCATION_PERMISSION_REQUEST_CODE = 100
        //private val UPDATE_INTERVAL = TimeUnit.SECONDS.toMinutes(15)
        private val UPDATE_INTERVAL = TimeUnit.SECONDS.toMillis(10)
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
                requestLocationUpdates()
                return
            }

            database = FirebaseDatabase.getInstance().reference

            fusedLocation.lastLocation.addOnSuccessListener {it
                if (it != null) {
                    val latitudes = it.latitude.toString()
                    val longitudes = it.longitude.toString()
                    Log.d("LocationService", "Latitude: $latitudes, Longitude: $longitudes")

                    if (latitudes.isEmpty() || longitudes.isEmpty()){
                        Log.e("Latitude dan Longitude null", "Error")
                    } else {
                        val userLatitude = latitudes.toDouble()
                        val userLongitude = longitudes.toDouble()

                        val db = FirebaseDatabase.getInstance().getReference("database")
                        val idRoute = db.push().key
                        val STD = modalLocation(idRoute.toString(),userLatitude, userLongitude)
                        database.child(idRoute.toString()).setValue(STD)
                        database.child(idRoute.toString()).setValue(STD).addOnCompleteListener {
                            Toast.makeText(applicationContext, "Succes", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Log.e("LocationService", "Last known location is null")
                }
            }
                .addOnFailureListener {
                    Log.e("LocationService", "Failed to get last location: ${it.message}")
                }
        }

        private fun requestLocationUpdates() {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
//                ActivityCompat.requestPermissions(
//                    this,
//                    arrayOf(
//                        Manifest.permission.ACCESS_FINE_LOCATION,
//                        Manifest.permission.ACCESS_COARSE_LOCATION
//                    ),
//                    LOCATION_PERMISSION_REQUEST_CODE
//                )
               //return
            }
            else {
                requestLocationUpdates()
                return
            }
            fusedLocation.requestLocationUpdates(
                createLocationRequest(),
                locationCallback,
                null /* Looper */
            )
        }
        private fun createLocationRequest(): LocationRequest {
            return LocationRequest.create().apply {
                interval = UPDATE_INTERVAL
                fastestInterval = UPDATE_INTERVAL / 2
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }
        }

        private val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    // Handle location update
                    val latitude = location.latitude
                    val longitude = location.longitude
                    Log.d("MainActivity", "Latitude: $latitude, Longitude: $longitude")
                }
            }
        }
    }
} */
