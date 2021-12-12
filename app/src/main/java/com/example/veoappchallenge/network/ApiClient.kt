package com.example.veoappchallenge.network

import com.example.veoappchallenge.model.DirectionsResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiClient {

    /**
     * This function calls the google maps directions api to retrieve the polyline for the suggested
     * route along with a few other useful pieces of data such as the bounds and legs of the route.
     */
    @GET("maps/api/directions/json")
     suspend fun getRouteDirections(
        @Query("mode") mode: String,
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("key") key: String
    ): Response<DirectionsResponse>
}