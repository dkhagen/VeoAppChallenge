package com.example.veoappchallenge.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.veoappchallenge.model.TripEntity

/**
 * This class defines how the database is built. We pass in the entities we want along with the
 * TypeConverter. If future releases added new columns/tables, we would create migrations here as
 * well.
 */

@Database(entities = [TripEntity::class], version = 1)
@TypeConverters(DataConverter::class)
abstract class TripDatabase : RoomDatabase() {

    abstract fun getTripDao(): TripDao

    companion object {
        private var instance: TripDatabase? = null

        fun getTripDatabase(context: Context): TripDatabase {
            if (instance == null) {
                instance = Room
                    .databaseBuilder(
                        context.applicationContext,
                        TripDatabase::class.java,
                        "trip-database"
                    )
                    .build()
            }
            return instance!!
        }
    }
}