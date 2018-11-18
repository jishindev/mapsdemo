package com.jishindev.android.mapsdemo

import android.content.SharedPreferences
import android.os.Handler
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.work.WorkManager
import com.google.android.gms.maps.model.LatLng
import com.jishindev.android.mapsdemo.network.ServerInterface
import com.jishindev.android.mapsdemo.utils.PreferenceHelper
import com.jishindev.android.mapsdemo.utils.location
import kotlinx.coroutines.*
import timber.log.Timber
import java.lang.Exception


class MainVM(
   // val workManager: WorkManager, val sharedPref: SharedPreferences,
    val api: ServerInterface
) : ViewModel()/*,
    SharedPreferences.OnSharedPreferenceChangeListener*/ {

    val ldLocation = MutableLiveData<LatLng>()

    private val handler = Handler()
    private var job: Job? = null
    private val fetchRunnable = object : Runnable {
        override fun run() {
            val fRunnable = this
            job?.cancel()
            job = GlobalScope.launch(Dispatchers.Main) {
                try {
                    val response = api.getLocation().await().body()
                    Timber.d("fetchRunnable: response is $response")
                    if (response != null) {
                        ldLocation.postValue(response.toLatLng())
                    }
                }catch (e:Exception){}
                handler.postDelayed(fRunnable, FETCH_INTERVAL)
            }
        }
    }

    fun startLocationFetching() {
        Timber.i("startLocationFetching() called")

        handler.post(fetchRunnable)

        /*sharedPref.registerOnSharedPreferenceChangeListener(this)


        val work = PeriodicWorkRequestBuilder<LatLngFetchWorker>(15, TimeUnit.SECONDS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()

        workManager.enqueueUniquePeriodicWork("fetch", ExistingPeriodicWorkPolicy.REPLACE, work)*/
    }

    fun stopLocationFetching() {
        Timber.i("stopLocationFetching() called")

        job?.cancel()
        handler.removeCallbacks(fetchRunnable)

        /*sharedPref.unregisterOnSharedPreferenceChangeListener(this)
        workManager.cancelAllWork()*/
    }

/*    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        Timber.i("onSharedPreferenceChanged, sharedPreferences: $sharedPreferences, key: $key")

        when (key) {
            PreferenceHelper.LOCATION -> {
                ldLocation.postValue(sharedPreferences?.location)
            }
        }
    }*/

    override fun onCleared() {
        super.onCleared()
        Timber.i("onCleared")
        stopLocationFetching()
    }

    companion object {
        const val FETCH_INTERVAL = 15_000L
    }
}