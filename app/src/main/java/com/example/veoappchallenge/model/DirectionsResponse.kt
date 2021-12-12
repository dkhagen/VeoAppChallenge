package com.example.veoappchallenge.model

import com.example.veoappchallenge.model.parsing.Route

/**
 * This data class allows us to parse out the route information we need from the Google
 * Directions API.
 */
data class DirectionsResponse(var routes: List<Route>)