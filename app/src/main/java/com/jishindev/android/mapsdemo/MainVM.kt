package com.jishindev.android.mapsdemo

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.maps.model.LatLng
import com.jishindev.android.mapsdemo.network.LatLngFetchWorker
import com.jishindev.android.mapsdemo.utils.PreferenceHelper
import com.jishindev.android.mapsdemo.utils.location
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

class MainVM : ViewModel(), SharedPreferences.OnSharedPreferenceChangeListener {

    // todo inject this
    private lateinit var workManager: WorkManager

    private lateinit var latLngFetchWorkId: UUID
    val ldLocation = MutableLiveData<LatLng>()

    init {
        // todo remove after injection
        workManager = WorkManager.getInstance()
    }


    fun startLocationFetching(){
        Timber.i("loadLocation() called")
        val work = PeriodicWorkRequestBuilder<LatLngFetchWorker>(15, TimeUnit.SECONDS).build()
        latLngFetchWorkId = work.id
        workManager.enqueue(work)
    }

    fun stopLocationFetching() {
        if (::latLngFetchWorkId.isInitialized) {
            workManager.cancelWorkById(latLngFetchWorkId)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        Timber.i("onSharedPreferenceChanged, sharedPreferences: $sharedPreferences, key: $key")

        when (key) {
            PreferenceHelper.LOCATION -> {
                ldLocation.postValue(sharedPreferences?.location)
            }
        }
    }
}