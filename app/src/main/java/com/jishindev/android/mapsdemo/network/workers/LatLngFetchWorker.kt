package com.jishindev.android.mapsdemo.network.workers

import android.content.Context
import android.content.SharedPreferences
import androidx.concurrent.futures.ResolvableFuture
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.common.util.concurrent.ListenableFuture
import com.jishindev.android.mapsdemo.network.ServerInterface
import com.jishindev.android.mapsdemo.utils.location
import kotlinx.coroutines.*
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import timber.log.Timber


class LatLngFetchWorker(
    context: Context, params: WorkerParameters
) : ListenableWorker(context, params), KoinComponent {

    private var job: Job? = null
    private val future: ResolvableFuture<Payload> = ResolvableFuture.create()

    override fun startWork(): ListenableFuture<Payload> {
        Timber.i("startWork() called")

        val sharedPref: SharedPreferences by inject()
        val api: ServerInterface by inject()

        Timber.i("startWork() called")

        job?.cancel()
        job = GlobalScope.launch {
            val response = async(Dispatchers.Default) {
                api.getLocation().await()
            }.await().body()

            Timber.d("startWork: response is $response")

            if (response != null)
            sharedPref.location = response.toLatLng().also {
                val outputData =
                    Data.Builder().putString("latlng", "${response.latitude},${response.longitude}").build()

                future.set(Payload(Result.SUCCESS, outputData))
            }
            else {
                Timber.e("startWork: response is null")
                future.set(Payload(Result.RETRY, Data.EMPTY))
            }
        }
        return future
    }


    override fun onStopped() {
        super.onStopped()
        job?.cancel()
        future.set(Payload(Result.FAILURE, Data.EMPTY))
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
