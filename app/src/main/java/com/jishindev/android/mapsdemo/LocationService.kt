package com.jishindev.android.mapsdemo

import android.Manifest
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Binder
import android.os.IBinder
import com.google.android.gms.location.*
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
class LocationService : Service() {

    // todo inject this
    private lateinit var fusedLocationProviderClient:FusedLocationProviderClient
    private lateinit var notificationManager: NotificationManager
    private var location: Location? = null
    private val binder = LocalBinder()
    private var isChangingConfiguration: Boolean = false

    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            super.onLocationResult(locationResult)
            locationResult ?: return

            location = locationResult.locations[0]
        }
    }

    override fun onCreate() {
        super.onCreate()

        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                location = it
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        stopForeground(true)
        isChangingConfiguration = false
        return binder
    }


    fun startLocationUpdates() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(LocationRequest(), locationCallback, null)
        }
    }

    fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    inner class LocalBinder : Binder() {
        val service: LocationService
            get() = this@LocationService
    }
}