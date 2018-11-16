package com.jishindev.android.mapsdemo

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.location.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    // todo inject
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val mainVm by lazy { ViewModelProviders.of(this).get(MainVM::class.java) }
    private var currentLocation: Location? = null
        set(value) {
            field = value; onNewLocation(value)
        }

    private var locationFetchService: LocationFetchService? = null
    private var isBound = false
    private var isFetchingLocations = false

    private val serviceConnection: ServiceConnection
        get() = object : ServiceConnection {

            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                Timber.i("onServiceConnected() called with: name = [$name], service = [$service]")
                val binder = service as LocationFetchService.LocalBinder
                locationFetchService = binder.fetchService
                isBound = true

                // start loading
            }

            override fun onServiceDisconnected(name: ComponentName) {
                Timber.i("onServiceDisconnected() called with: name = [$name]")
                locationFetchService = null
                isBound = false
            }
        }


    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            super.onLocationResult(locationResult)
            locationResult ?: return
            currentLocation = locationResult.locations[0]
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // todo remove after injection
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fab.setOnClickListener {
            if (isFetchingLocations) {
                fab.setImageResource(R.drawable.ic_play_arrow_black_24dp)

            } else {
                fab.setImageResource(R.drawable.ic_stop_black_24dp)
            }
        }

        loadLastKnownLocation()

        mainVm.ldLocation.observe(this, Observer {
            googleMap.addMarker(MarkerOptions().position(it.toLatLng()))
        })

    }

    override fun onStart() {
        super.onStart()

        val serviceIntent = Intent(this, LocationFetchService::class.java)
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)

        startLocationUpdates()
    }

    override fun onStop() {
        super.onStop()

        if (isBound) {
            try {
                unbindService(serviceConnection)
                isBound = false
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        stopLocationUpdates()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

    }

    private fun onNewLocation(location: Location?) {
        Timber.i("onNewLocation() called with: location = [$location]")
        location ?: return
    }

    private fun loadLastKnownLocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                currentLocation = it
            }
        }
    }

    private fun startLocationUpdates() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(LocationRequest(), locationCallback, null)
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }
}
