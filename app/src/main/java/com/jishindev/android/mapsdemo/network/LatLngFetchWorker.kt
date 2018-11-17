package com.jishindev.android.mapsdemo.network

import android.content.Context
import android.content.SharedPreferences
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.jishindev.android.mapsdemo.utils.location
import kotlinx.coroutines.*

class LatLngFetchWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    // todo inject these
    private lateinit var sharedPref:SharedPreferences
    private lateinit var api:ServerInterface

    private var job: Job? = null
    override fun doWork(): Result {

        job?.cancel()
        job = GlobalScope.launch {
            val location = async(Dispatchers.Default) {
                api.getLocation().await()
            }.await().body()

            if (location != null)
                sharedPref.location = location.toLatLng()
        }

        return Result.FAILURE
    }

    override fun onStopped() {
        super.onStopped()
        job?.cancel()
    }
}