package com.example.veoappchallenge

import android.app.Application
import com.example.veoappchallenge.di.BaseComponent
import com.example.veoappchallenge.di.DaggerBaseComponent
import com.example.veoappchallenge.di.DatabaseModule
import com.example.veoappchallenge.di.NetworkModule

// This application class lets us set the component class from dagger to enable dependency injection
class MapsApplication : Application() {

    private lateinit var baseComponent: BaseComponent

    override fun onCreate() {
        super.onCreate()
        baseComponent = DaggerBaseComponent.builder()
            .networkModule(NetworkModule())
            .databaseModule(DatabaseModule(this))
            .build()
    }

    fun getBaseComponent(): BaseComponent {
        return baseComponent
    }
}