package com.jishindev.android.mapsdemo.models

import com.google.android.gms.maps.model.LatLng

data class LocationModel(var location: String) {

    fun toLatLng() = LatLng(0.0, 0.0)
}