package com.example.veoappchallenge.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng

/**
 * This is a Room entity object representing the Trip object. This format allows us to more easily
 * store a Trip in the database.
 */
@Entity(tableName = "trips")
data class TripEntity(
    @ColumnInfo(name = "start_time") val startTime: Long,
    @ColumnInfo(name = "start_lat") val startLat: Double?,
    @ColumnInfo(name = "start_lng") val startLng: Double?,
    @ColumnInfo(name = "destination_lat") val destinationLat: Double?,
    @ColumnInfo(name = "destination_lng") val destinationLng: Double?,
    @ColumnInfo(name = "duration") val duration: Int?,
    @ColumnInfo(name = "total_distance") val totalDistance: Float?,
    @ColumnInfo(name = "location_list") val locationList: MutableList<LatLng>?
) {
    @PrimaryKey(autoGenerate = true) var uid: Int = 0
}
