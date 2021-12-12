package com.example.veoappchallenge.utils

import android.location.Location
import android.widget.TextView
import com.example.veoappchallenge.model.Trip
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap

/**
 * This is a utils class that helps process location data and convert the data into a more
 * user friendly format (e.g. m/s -> mph)
 */
class LocationUtils {

    companion object {
        // Conversion factors
        private const val DIVIDE_TO_GET_FEET_FROM_METERS = 0.3048f
        private const val DIVIDE_TO_GET_MILES_FROM_METERS = 1609.34f
        const val POLYLINE_WIDTH_PX = 20f

        /**
         * This handles updating the users location while on a trip. It is set for a 3 second update
         * period using high accuracy and a minimum displacement of 3 meters. I believe Veo currently
         * uses a 5 second update period.
         */
        fun createLocationRequest(): LocationRequest {
            return LocationRequest.create().apply {
                interval = 3000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                smallestDisplacement = 3f
            }
        }

        /**
         * This callback is the main driver that tracks the user's progress and draws the user's
         * path as a polyline on the map. It first checks if the user is close enough to their
         * destination and will end the trip if so. Otherwise, it calculates the distance travelled
         * since the last update and adds it to the Trip's running total. It also updates the
         * necessary TextViews and moves the camera to follow the user.
         */
        fun getLocationCallback(
            currentTrip: Trip?,
            map: GoogleMap,
            color: Int,
            tvDistanceTraveled: TextView,
            tvDistanceRemaining: TextView,
            endTripFunction: () -> Unit
        ): LocationCallback {
            return object : LocationCallback() {
                override fun onLocationResult(locations: LocationResult) {
                    for (location in locations.locations) {
                        val latlng = LatLng(location.latitude, location.longitude)
                        if (currentTrip?.hasArrivedAtDestination() == true) {
                            endTripFunction()
                        }
                        val lastLocation = currentTrip?.getLastLocation()
                        if (lastLocation != null) {
                            val distanceTraveled = FloatArray(2)
                            Location.distanceBetween(
                                lastLocation.latitude,
                                lastLocation.longitude,
                                latlng.latitude,
                                latlng.longitude,
                                distanceTraveled
                            )

                            currentTrip.addDistance(distanceTraveled[0])
                            tvDistanceTraveled.text = updateDistanceText(currentTrip.totalDistance, false)
                            tvDistanceRemaining.text =
                                updateDistanceText(currentTrip.distanceRemaining, true)
                            map.addPolyline(
                                PolylineOptions().add(lastLocation).add(latlng).color(color)
                                    .width(POLYLINE_WIDTH_PX).jointType(JointType.ROUND)
                                    .endCap(RoundCap())
                            )
                        }
                        currentTrip?.addLocation(latlng)
                        map.animateCamera(CameraUpdateFactory.newLatLng(latlng))
                    }
                }
            }
        }

        // A helper function to convert meters to feet and miles.
        fun updateDistanceText(distance: Float?, showRemaining: Boolean): String {
            return if (distance != null && showRemaining) {
                when (distance) {
                    in 0f..400f -> "%.0f ft remaining".format(convertMetersToFeet(distance))
                    else -> "%.1f miles remaining".format(convertMetersToMiles(distance))
                }
            } else if (distance != null && !showRemaining) {
                when (distance) {
                    in 0f..400f -> "%.0f ft".format(convertMetersToFeet(distance))
                    else -> "%.1f miles".format(convertMetersToMiles(distance))
                }
            } else {
                "- -"
            }
        }

        private fun convertMetersToMiles(meters: Float): Float {
            return meters / DIVIDE_TO_GET_MILES_FROM_METERS
        }

        private fun convertMetersToFeet(meters: Float): Float {
            return meters / DIVIDE_TO_GET_FEET_FROM_METERS
        }
    }
}