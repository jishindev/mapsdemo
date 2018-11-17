package com.jishindev.android.mapsdemo.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import androidx.annotation.CheckResult
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import timber.log.Timber


fun Location.toLatLng() = LatLng(latitude, longitude)
fun String?.toLatLng(): LatLng? {

    this ?: return null

    if (!isNullOrEmpty() && contains(",")) {
        val latLngArray = split(",").map { it.toDouble() }
        return LatLng(latLngArray[0], latLngArray[1])
    }

    return null
}


@CheckResult(suggest = "#enforceCallingOrSelfPermission()")
fun Context.ifLocationPermsGranted(block: () -> Unit): Boolean {
    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        block()
        return true
    }
    return false
}

fun Activity.checkAndDisplayLocationSettings(requestCode: Int = 1000, action: () -> Unit) {
    Timber.i("checkAndDisplayLocationSettings, requestCode: $requestCode, action: $action")

    val locationRequest = LocationRequest.create()
    locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    locationRequest.interval = 3000
    locationRequest.fastestInterval = 1000

    val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
    builder.setAlwaysShow(true)

    LocationServices.getSettingsClient(this).checkLocationSettings(builder.build())
        .addOnCompleteListener {
            try {
                it.getResult(ApiException::class.java)
                action()
            } catch (e: ApiException) {
                when (e.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        try {
                            (e as ResolvableApiException).startResolutionForResult(
                                this@checkAndDisplayLocationSettings,
                                requestCode
                            )

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }

}

fun Context.getBitmapFromVector(
    @DrawableRes vectorResourceId: Int,
    @ColorInt tintColor: Int = -1
): BitmapDescriptor {

    val vectorDrawable = ResourcesCompat.getDrawable(
        resources, vectorResourceId, null
    )
    if (vectorDrawable == null) {
        Timber.e("Requested vector resource was not found")
        return BitmapDescriptorFactory.defaultMarker()
    }
    val bitmap = Bitmap.createBitmap(
        vectorDrawable.intrinsicWidth,
        vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
    if (tintColor != -1) {
        DrawableCompat.setTint(vectorDrawable, tintColor)
    }
    vectorDrawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}
