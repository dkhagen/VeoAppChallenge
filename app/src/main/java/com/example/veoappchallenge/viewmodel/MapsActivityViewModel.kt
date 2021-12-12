package com.example.veoappchallenge.viewmodel

import android.app.Application
import android.content.DialogInterface
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.veoappchallenge.MapsApplication
import com.example.veoappchallenge.database.TripDao
import com.example.veoappchallenge.model.DirectionsResponse
import com.example.veoappchallenge.model.Trip
import com.example.veoappchallenge.model.TripEntity
import com.example.veoappchallenge.model.parsing.BoundsCoordinates
import com.example.veoappchallenge.network.ApiClient
import com.example.veoappchallenge.utils.LocationUtils
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.*
import javax.inject.Inject

/**
 * This is the primary view model for the app. It handles the Directions API call as well as
 * database insertions.
 */
class MapsActivityViewModel(application: Application) : AndroidViewModel(application),
    CoroutineScope by MainScope() {

    @Inject
    lateinit var apiService: ApiClient

    @Inject
    lateinit var tripDao: TripDao

    private val routesLiveDataList: MutableLiveData<DirectionsResponse> = MutableLiveData()
    private var swBounds: BoundsCoordinates? = null
    private var neBounds: BoundsCoordinates? = null

    init {
        // Inject this class to get the ApiClient and TripDao.
        (application as MapsApplication).getBaseComponent().inject(this)
    }

    fun getDirectionsResponseData(): MutableLiveData<DirectionsResponse> {
        return routesLiveDataList
    }

    fun getStoreTripOnClick(trip: Trip): DialogInterface.OnClickListener {
        val tripEntity = TripEntity(
            trip.startTime,
            trip.start?.latitude,
            trip.start?.longitude,
            trip.destination?.latitude,
            trip.destination?.longitude,
            trip.duration,
            trip.totalDistance,
            trip.locationList
        )
        return DialogInterface.OnClickListener { _, _ ->
            addTrip(tripEntity)
        }
    }

    private fun addTrip(trip: TripEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            tripDao.insertAll(trip)
        }
    }

    /**
     * This function calls the Directions api using coroutines. The mode is hard coded to "walking"
     * since I believe that makes the most sense for scooters. If the response is successful, we
     * post the value to the live data object.
     */
    fun callDirectionsApi(origin: String, destination: String, key: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = apiService.getRouteDirections(
                "walking",
                origin,
                destination,
                key
            )
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    routesLiveDataList.postValue(response.body())
                } else {
                    routesLiveDataList.postValue(null)
                }
            }
        }
    }

    /**
     * Helper function that creates a bounded polyline based on the over view polyline returned by
     * the Directions API call.
     */
    fun createPolyLineOptions(
        directionsResponse: DirectionsResponse,
        routeColor: Int
    ): PolylineOptions {
        val options = PolylineOptions()
        for (route in directionsResponse.routes) {
            options.addAll(PolyUtil.decode(route.overViewPolyLine.points))
            swBounds = route.bounds.southwest
            neBounds = route.bounds.northeast
        }
        options.color(routeColor).endCap(RoundCap())
            .width(LocationUtils.POLYLINE_WIDTH_PX).jointType(JointType.ROUND)
        return options
    }

    // Creates a camera update based on the bounds provided.
    fun createCameraUpdate(padding: Int): CameraUpdate {
        val localSwBounds = swBounds
        val localNeBounds = neBounds
        return if (localSwBounds != null && localNeBounds != null) {
            CameraUpdateFactory.newLatLngBounds(
                LatLngBounds.Builder()
                    .include(LatLng(localSwBounds.lat, localSwBounds.lng))
                    .include(LatLng(localNeBounds.lat, localNeBounds.lng))
                    .build(),
                padding
            )
        } else {
            CameraUpdateFactory.newLatLngZoom(LatLng(DEFAULT_LAT, DEFAULT_LNG), DEFAULT_ZOOM)
        }
    }

    // Default values in case the api call fails.
    companion object {
        const val DEFAULT_LAT = 38.897957
        const val DEFAULT_LNG = -77.036560
        const val DEFAULT_ZOOM = 14f
    }
}