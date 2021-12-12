package com.example.veoappchallenge.model.parsing

import com.google.gson.annotations.SerializedName

data class BoundsCoordinates(
    @SerializedName("lat")
    var lat: Double,
    @SerializedName("lng")
    var lng: Double
)
