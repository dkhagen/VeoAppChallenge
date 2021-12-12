package com.example.veoappchallenge.model.parsing

import com.google.gson.annotations.SerializedName

data class RouteBounds(
    @SerializedName("northeast")
    var northeast: BoundsCoordinates,
    @SerializedName("southwest")
    var southwest: BoundsCoordinates
)
