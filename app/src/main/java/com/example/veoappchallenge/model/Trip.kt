package com.example.veoappchallenge.model

import android.location.Location
import com.google.android.gms.maps.model.LatLng

/**
 * This class represents a Trip being taken by the user. It contains start and end LatLng points
 * as well a list of LatLng points representing the location updates during the trip. It tracks
 * the total duration and distance travelled as well.
 */
class Trip(val start: LatLng?, val destination: LatLng?) {
    val locationList: MutableList<LatLng>
    var duration: Int
    var totalDistance: Float
    var distanceRemaining: Float?
    var startTime: Long = 0

    init {
        locationList = ArrayList()
        duration = 0
        totalDistance = 0f
        distanceRemaining = getDistanceToDestination()
    }

    // Pull the most recently added LatLng point from the list
    fun getLastLocation(): LatLng? {
        return if (locationList.size > 0) {
            return locationList.last()
        } else {
            start
        }
    }

    /**
     * This function will be called each time the user's location is updated. It will check if the
     * user is within 15 meters of their destination. If so it will return true.
     */
    fun hasArrivedAtDestination(): Boolean {
        val currentDistanceRemaining = getDistanceToDestination()
        return if (currentDistanceRemaining != null) {
            distanceRemaining = currentDistanceRemaining
            currentDistanceRemaining < 15f
        } else {
            false
        }
    }

    // A helper function to calculate the remaining distance to the destination
    private fun getDistanceToDestination(): Float? {
        val lastLocation = getLastLocation()
        val resultsArray = FloatArray(2)
        if (lastLocation != null && destination != null) {
            Location.distanceBetween(
                lastLocation.latitude,
                lastLocation.longitude,
                destination.latitude,
                destination.longitude,
                resultsArray
            )
            return resultsArray.first()
        }
        return null
    }

    fun addDistance(newSegment: Float) {
        totalDistance += newSegment
    }

    fun addLocation(newLocation: LatLng) {
        locationList.add(newLocation)
    }
}