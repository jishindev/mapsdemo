package com.jishindev.android.mapsdemo

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.jishindev.android.mapsdemo.utils.PreferenceHelper
import com.jishindev.android.mapsdemo.utils.checkAndDisplayLocationSettings
import com.jishindev.android.mapsdemo.utils.isRequestingLocationUpdates
import com.jishindev.android.mapsdemo.utils.toLatLng
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    // todo inject
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var sharedPref: SharedPreferences

    private val mainVm by lazy { ViewModelProviders.of(this).get(MainVM::class.java) }
    private var currentLocation: Location? = null
        set(value) {
            field = value; /*onNewLatLng(value?.toLatLng())*/
        }

    private var isBound = false
    private var marker: Marker? = null

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
        sharedPref = PreferenceHelper.prefs(this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)


        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fabStartStop?.setOnClickListener {
            toggleLocationFetch()
        }

        loadLastKnownLocation()

        mainVm.ldLocation.observe(this, Observer {
            onNewLatLng(it)
        })
    }

    override fun onStart() {
        super.onStart()

        checkAndDisplayLocationSettings {
            startLocationUpdates()
        }
    }

    override fun onStop() {
        super.onStop()

        stopLocationUpdates()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
    }

    private fun toggleLocationFetch() {
        Timber.i("toggleLocationFetch")
        if (sharedPref.isRequestingLocationUpdates) {
            mainVm.stopLocationFetching()
            fabStartStop.setImageResource(R.drawable.ic_play_arrow_white_24dp)
        } else {
            mainVm.startLocationFetching()
            fabStartStop.setImageResource(R.drawable.ic_stop_white_24dp)
        }

        sharedPref.isRequestingLocationUpdates = !sharedPref.isRequestingLocationUpdates
    }

    private fun onNewLatLng(latLng: LatLng?) {
        Timber.i("onNewLatLng, latLng: $latLng")
        latLng ?: return

        // draw/update the marker
        if (marker == null) {
            marker = googleMap.addMarker(MarkerOptions().position(latLng))
        } else {
            marker?.position = latLng
        }

        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 13f)
        googleMap.animateCamera(cameraUpdate)
    }

    private fun loadLastKnownLocation() {
        Timber.i("loadLastKnownLocation")
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                currentLocation = it
            }
        }
    }

    private fun startLocationUpdates() {
        Timber.i("startLocationUpdates")
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(LocationRequest(), locationCallback, null)
        }
    }

    private fun stopLocationUpdates() {
        Timber.i("stopLocationUpdates")
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }
}
