package com.jishindev.android.mapsdemo.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.CheckResult
import kotlin.contracts.ExperimentalContracts


@CheckResult(suggest = "#enforceCallingOrSelfPermission()")
fun Context.ifLocationPermsGranted(block: () -> Unit): Boolean {
    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        block()
        return true
    }
    return false
}