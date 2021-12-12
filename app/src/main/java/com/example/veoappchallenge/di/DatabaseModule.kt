package com.example.veoappchallenge.di

import android.app.Application
import android.content.Context
import com.example.veoappchallenge.database.TripDao
import com.example.veoappchallenge.database.TripDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * This module defines the functions required to inject the Database into the view model.
 */

@Module
class DatabaseModule(val application: Application) {

    @Provides
    @Singleton
    fun getTripDao(tripDatabase: TripDatabase): TripDao {
        return tripDatabase.getTripDao()
    }

    @Provides
    @Singleton
    fun getDatabaseInstance(): TripDatabase {
        return TripDatabase.getTripDatabase(providesContext())
    }

    @Provides
    @Singleton
    fun providesContext(): Context {
        return application.applicationContext

    }
}