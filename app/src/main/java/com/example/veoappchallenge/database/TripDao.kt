package com.example.veoappchallenge.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.veoappchallenge.model.TripEntity

/**
 * The DAO object that allows the view models to interact with the database.
 */

@Dao
interface TripDao {
    @Query("SELECT * FROM trips")
    suspend fun getAllTrips(): List<TripEntity>

    @Insert
    suspend fun insertAll(vararg trips: TripEntity)

    @Query("DELETE FROM trips")
    suspend fun deleteAll()

    @Query("DELETE FROM trips WHERE uid = :uid")
    suspend fun deleteTrip(uid: Int)
}