package com.jishindev.android.mapsdemo.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.android.gms.maps.model.LatLng
import com.jishindev.android.mapsdemo.utils.PreferenceHelper.get
import com.jishindev.android.mapsdemo.utils.PreferenceHelper.set


object PreferenceHelper {

    const val IS_REQUESTING_LOCATION_UPDATES = "is_requesting_location_updates"
    const val LOCATION = "location"


    fun prefs(context: Context, name: String = ""): SharedPreferences =
        context.getSharedPreferences(name, Context.MODE_PRIVATE)

    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = this.edit()
        operation(editor)
        editor.apply()
    }


    operator fun SharedPreferences.set(key: String, value: Any?) {
        when (value) {
            is String? -> edit { it.putString(key, value) }
            is Int -> edit { it.putInt(key, value) }
            is Boolean -> edit { it.putBoolean(key, value) }
            is Float -> edit { it.putFloat(key, value) }
            is Long -> edit { it.putLong(key, value) }
            else -> throw UnsupportedOperationException("Not yet implemented")
        }
    }

    inline operator fun <reified T : Any> SharedPreferences.get(key: String, defaultValue: T? = null): T? {

        return when (T::class) {
            String::class -> getString(key, defaultValue as? String ?: "") as T?
            Int::class -> getInt(key, defaultValue as? Int ?: 0) as T?
            Boolean::class -> getBoolean(key, defaultValue as? Boolean ?: false) as T?
            Float::class -> getFloat(key, defaultValue as? Float ?: 0f) as T?
            Long::class -> getLong(key, defaultValue as? Long ?: 0) as T?
            else -> throw UnsupportedOperationException("Not yet implemented")
        }
    }
}

var SharedPreferences.isRequestingLocationUpdates: Boolean
    get() = this[PreferenceHelper.IS_REQUESTING_LOCATION_UPDATES] ?: false
    set(value) {
        this[PreferenceHelper.IS_REQUESTING_LOCATION_UPDATES] = value
    }

var SharedPreferences.location: LatLng?
    get() {
        val locString: String? = this[PreferenceHelper.LOCATION]
        return locString?.toLatLng()
    }
    set(value) {
        if (value != null)
            this[PreferenceHelper.LOCATION] = "${value.latitude},${value.longitude}"
    }
