package com.example.veoappchallenge.di

import com.example.veoappchallenge.viewmodel.MapsActivityViewModel
import com.example.veoappchallenge.viewmodel.TripsActivityViewModel
import dagger.Component
import javax.inject.Singleton

/**
 * This component class tells Dagger where we plan on using the listed modules by defining inject
 * functions with the view model as the parameter.
 */

@Singleton
@Component(
    modules = [NetworkModule::class, DatabaseModule::class]
)
interface BaseComponent {
    fun inject(mapsActivityViewModel: MapsActivityViewModel)
    fun inject(tripsActivityViewModel: TripsActivityViewModel)
}