package com.example.veoappchallenge.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.veoappchallenge.MapsApplication
import com.example.veoappchallenge.database.TripDao
import com.example.veoappchallenge.model.TripEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * This view model class handles retrieving and deleting TripEntity records from the database using
 * coroutines.
 */
class TripsActivityViewModel(application: Application) : AndroidViewModel(application) {

    @Inject
    lateinit var tripDao: TripDao
    val liveDataTripList: MutableLiveData<List<TripEntity>> = MutableLiveData()

    init {
        // Inject this class to get the TripDao
        (application as MapsApplication).getBaseComponent().inject(this)
    }

    fun getAllTrips() {
        CoroutineScope(Dispatchers.IO).launch {
            val tripList = tripDao.getAllTrips()
            liveDataTripList.postValue(tripList)
        }
    }

    fun deleteAllTrips() {
        CoroutineScope(Dispatchers.IO).launch {
            tripDao.deleteAll()
            // After deletion, get all trips again to force live data
            getAllTrips()
        }
    }

    fun deleteTrip(tripEntity: TripEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            tripDao.deleteTrip(tripEntity.uid)
            // After deletion, get all trips again to force live data
            getAllTrips()
        }
    }
}