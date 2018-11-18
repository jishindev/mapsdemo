package com.jishindev.android.mapsdemo

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.fondesa.kpermissions.extension.listeners
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.jishindev.android.mapsdemo.utils.*
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private val fusedLocationProviderClient: FusedLocationProviderClient by inject()
    private val sharedPref: SharedPreferences by inject()
    private val mainVm: MainVM by viewModel()

    private var marker: Marker? = null
    private var currentLocMarker: Marker? = null
    private lateinit var googleMap: GoogleMap

    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            super.onLocationResult(locationResult)
            locationResult ?: return

            // stop location updates once user location is received
            stopLocationUpdates()

            // mark it on the map
            showCurrentLocation(locationResult.locations[0])
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fabStartStop?.setOnClickListener { toggleLocationFetch() }

        // observe for the latLng fetch that happens every 15 seconds
        mainVm.ldLocation.observe(this, Observer {
            Timber.i("onCreate mainVm.ldLocation : $it")
            onNewLatLng(it)
        })

        if (sharedPref.isRequestingLocationUpdates) {
            mainVm.startLocationFetching()
            fabStartStop.setImageResource(R.drawable.ic_stop_white_24dp)
        }

        loadLastKnownLocation()
    }

    override fun onStart() {
        super.onStart()

        // request for location access
        val request = permissionsBuilder(Manifest.permission.ACCESS_FINE_LOCATION).build()
        request.listeners {
            onAccepted {
                checkAndDisplayLocationSettings {
                    startLocationUpdates()
                }
            }

            onDenied {
                Timber.e("onStart: Location access denied")
            }
        }

        request.send()
    }

    override fun onStop() {
        super.onStop()

        stopLocationUpdates()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            1000 -> {
                if (resultCode == Activity.RESULT_OK) {
                    startLocationUpdates()
                }
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
    }

    private fun toggleLocationFetch() {
        Timber.i("toggleLocationFetch() called")
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
            marker = googleMap.addMarker(
                MarkerOptions()
                    .title(getString(R.string.live_location))
                    .position(latLng)
                    .icon(getBitmapFromVector(R.drawable.ic_place_green_24dp))
            )
        } else {
            marker?.position = latLng
        }

        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10f)
        googleMap.animateCamera(cameraUpdate)
    }

    private fun loadLastKnownLocation() {
        Timber.i("loadLastKnownLocation() called")
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                showCurrentLocation(it)
            }
        }
    }

    private fun showCurrentLocation(location: Location?) {
        Timber.i("showCurrentLocation() called with: location = [$location]")
        location ?: return

        if (::googleMap.isInitialized) {
            if (currentLocMarker != null) {
                currentLocMarker?.position = location.toLatLng()
            } else {
                currentLocMarker =
                        googleMap.addMarker(
                            MarkerOptions()
                                .title(getString(R.string.current_location))
                                .position(location.toLatLng())
                                .icon(getBitmapFromVector(R.drawable.ic_person_pin_circle_blue_24dp))
                        )
            }


            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(location.toLatLng(), 10f)
            googleMap.animateCamera(cameraUpdate)
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
