package com.jishindev.android.mapsdemo

import android.content.SharedPreferences
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.android.gms.maps.model.LatLng
import com.jishindev.android.mapsdemo.network.workers.LatLngFetchWorker
import com.jishindev.android.mapsdemo.utils.PreferenceHelper
import com.jishindev.android.mapsdemo.utils.location
import timber.log.Timber
import java.util.concurrent.TimeUnit


class MainVM(val workManager: WorkManager, val sharedPref: SharedPreferences) : ViewModel(),
    SharedPreferences.OnSharedPreferenceChangeListener {


    val ldLocation = MutableLiveData<LatLng>()
    private lateinit var lifecycleOwner: LifecycleOwner
    fun setLifeCycleOwner(lOwner: LifecycleOwner) {
        lifecycleOwner = lOwner
    }

    fun startLocationFetching() {
        Timber.i("startLocationFetching() called")

        sharedPref.registerOnSharedPreferenceChangeListener(this)

        val work = PeriodicWorkRequestBuilder<LatLngFetchWorker>(15, TimeUnit.SECONDS).addTag(WORKER_TAG).build()
        //latLngFetchWorkId = work.id
        workManager.enqueue(work)


        if (::lifecycleOwner.isInitialized) {
            workManager.getWorkInfosByTag(WORKER_TAG).addListener({

               /* Timber.i("startLocationFetching: work: ${it.state}")
                when (it?.state) {

                    WorkInfo.State.SUCCEEDED -> {
                        ldLocation.value = it.outputData.getString("latlng")?.toLatLng().also {
                            Timber.d("startLocationFetching: latlng: $it")
                        }
                    }
                    else -> {
                        Timber.i("startLocationFetching: work: ${it.state}")
                    }
                }*/
            }, {

            })
        } else {
            throw IllegalStateException("Set a lifecycle owner to the VM")
        }
    }

    fun stopLocationFetching() {
        Timber.i("stopLocationFetching() called")

        sharedPref.unregisterOnSharedPreferenceChangeListener(this)
        workManager.cancelAllWorkByTag(WORKER_TAG)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        Timber.i("onSharedPreferenceChanged, sharedPreferences: $sharedPreferences, key: $key")

        when (key) {
            PreferenceHelper.LOCATION -> {
                ldLocation.value = sharedPref.location
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationFetching()
    }

    companion object {

        const val WORKER_TAG = "fetchWork"
    }
}