package com.jishindev.android.mapsdemo.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.annotation.CheckResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.model.LatLng
import timber.log.Timber


fun Location.toLatLng() = LatLng(latitude,longitude)


@CheckResult(suggest = "#enforceCallingOrSelfPermission()")
fun Context.ifLocationPermsGranted(block: () -> Unit): Boolean {
    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        block()
        return true
    }
    return false
}

fun Activity.checkAndDisplayLocationSettings(requestCode: Int = 0, action: () -> Unit) {
    Timber.i("checkAndDisplayLocationSettings, requestCode: $requestCode, action: $action")

    val locationRequest = LocationRequest.create()
    locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    locationRequest.interval = 3000
    locationRequest.fastestInterval = 1000

    val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
    builder.setAlwaysShow(true)

    LocationServices.getSettingsClient(this).checkLocationSettings(builder.build())
        .addOnSuccessListener {
            Timber.i("checkAndDisplayLocationSettings success")
            action()
        }.addOnFailureListener {
            Timber.i("checkAndDisplayLocationSettings failure")
        }.addOnCanceledListener {
            Timber.i("checkAndDisplayLocationSettings cancelled")
        }
    /* r.addOnCompleteListener { task->
         val status = task.result
         when (status.statusCode) {
             LocationSettingsStatusCodes.SUCCESS -> Timber.i("All location settings are satisfied.")
             LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                 Timber.i(
                     "Location settings are not satisfied. Show the user a dialog to upgrade location settings "
                 )

                 try {
                     // Show the dialog by calling startResolutionForResult(), and check the result
                     // in onActivityResult().
                     status.startResolutionForResult(this, requestCode)
                 } catch (e: IntentSender.SendIntentException) {
                     Timber.i("PendingIntent unable to execute request.")
                 }

             }
             LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> Timber.i(
                 "Location settings are inadequate, and cannot be fixed here. Dialog not created."
             )
         }*/
}