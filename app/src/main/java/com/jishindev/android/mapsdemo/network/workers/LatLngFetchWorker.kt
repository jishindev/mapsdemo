package com.jishindev.android.mapsdemo.network.workers

import android.content.Context
import android.content.SharedPreferences
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.jishindev.android.mapsdemo.models.LatLngResponse
import com.jishindev.android.mapsdemo.network.ServerInterface
import com.jishindev.android.mapsdemo.utils.location
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import timber.log.Timber


class LatLngFetchWorker(
    context: Context, params: WorkerParameters
) : Worker(context, params), KoinComponent, SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        Timber.i("onSharedPreferenceChanged, sharedPreferences: $sharedPreferences, key: $key")
    }

    private val sharedPref: SharedPreferences by inject()
    private val api: ServerInterface by inject()

    override fun doWork(): ListenableWorker.Result {
        Timber.i("startWork() called")

        sharedPref.registerOnSharedPreferenceChangeListener(this)

        val response: LatLngResponse? = null//api.getLocation().execute().body()
        Timber.d("startWork: response is $response")

        return if (response != null) {

            sharedPref.location = response.toLatLng()

            outputData = Data.Builder().putString(
                "latlng",
                "${response.latitude},${response.longitude}"
            ).build()

            Result.SUCCESS
        } else {
            Timber.e("startWork: response is null")
            Result.FAILURE
        }
    }

    override fun onStopped() {
        super.onStopped()
        Timber.i("onStopped")
        sharedPref.unregisterOnSharedPreferenceChangeListener(this)
    }
}


/*
class LatLngFetchWorker(
    context: Context, params: WorkerParameters
) : Worker(context, params), KoinComponent {

    private var job: Job? = null
    override fun doWork(): Result {

        val sharedPref: SharedPreferences by inject()
        val api: ServerInterface by inject()

        Timber.i("doWork() called")

        job?.cancel()
        job = GlobalScope.launch {
            val response = async(Dispatchers.Default) {
                api.getLocation().await()
            }.await().body()

            Timber.d("doWork: response is $response")

            if (response != null)
                */
/*sharedPref.location =*//*
 response.toLatLng().also {
                    outputData =
                            Data.Builder().putString("latlng", "${response.latitude},${response.longitude}").build()
                }
            else
                Timber.e("doWork: response is null")
        }

        return Result.SUCCESS
    }

    override fun onStopped() {
        super.onStopped()
        job?.cancel()
    }
}*/
