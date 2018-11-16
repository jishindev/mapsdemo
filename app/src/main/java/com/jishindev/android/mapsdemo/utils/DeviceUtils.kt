package com.jishindev.android.mapsdemo.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract


@ExperimentalContracts
fun Context.ifLocationPermsGranted(block: () -> Unit) {
    /*contract {

    }*/
    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        block()
    }
}