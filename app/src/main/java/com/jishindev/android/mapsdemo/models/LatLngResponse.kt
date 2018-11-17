package com.jishindev.android.mapsdemo.models

import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName

data class LatLngResponse(
    @SerializedName("status") val status: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double
) {

    fun toLatLng() = LatLng(latitude, longitude)
}