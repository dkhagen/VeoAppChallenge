package com.example.veoappchallenge.database

import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson

/**
 * A DataConverter class is necessary to store Objects in a room database. Gson allows us to
 * easily convert complex objects with minimal code.
 */
class DataConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromString(value: String): List<LatLng> {
        return gson.fromJson(value, Array<LatLng>::class.java).asList()
    }

    @TypeConverter
    fun fromLatLng(latLng: List<LatLng>): String {
        return gson.toJson(latLng)
    }
}