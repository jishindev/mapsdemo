package com.jishindev.android.mapsdemo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jishindev.android.mapsdemo.models.LocationModel
import com.jishindev.android.mapsdemo.network.ServerInterface
import kotlinx.coroutines.*
import timber.log.Timber

class MainVM : ViewModel() {


    // todo inject this
    private val api = ServerInterface.getApi()

    var ldLocation = MutableLiveData<LocationModel>()
    private var job: Job? = null


    fun loadLocation() {
        Timber.i("loadLocation() called")

        job?.cancel()
        job = GlobalScope.launch {
            val location = async(Dispatchers.Default) {
                api.getLocation().await()
            }.await().body()

            if (location != null)
                ldLocation.postValue(location)
        }

    }


    override fun onCleared() {
        super.onCleared()
        Timber.i("onCleared() called")
        job?.cancel()
    }
}