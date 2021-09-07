package com.intergroupapplication.presentation.delegate

import android.content.SharedPreferences
import com.google.gson.Gson
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Created by abakarmagomedov on 06/08/2018 at project InterGroupApplication.
 */
class SharedPrefDelegate<O, T>(
    private val type: Class<T>,
    private val gson: Gson,
    private val sharedPreferences: SharedPreferences
) : ReadWriteProperty<O, T?> {

    var value: T? = null

    override fun getValue(thisRef: O, property: KProperty<*>): T? {
        if (value == null) {
            value = gson.fromJson(sharedPreferences.getString(type.name, null), type)
        }

        return value
    }

    override fun setValue(thisRef: O, property: KProperty<*>, value: T?) {
        if (value != null) {
            sharedPreferences.edit().putString(type.name, gson.toJson(value)).apply()
        }

        this.value = value
    }
}
