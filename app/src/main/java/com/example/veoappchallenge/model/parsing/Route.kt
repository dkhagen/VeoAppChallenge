package com.example.veoappchallenge.model.parsing

import com.google.gson.annotations.SerializedName

data class Route(
    @SerializedName("overview_polyline")
    var overViewPolyLine: OverViewPolyLine,
    @SerializedName("legs")
    var legs: List<Leg>,
    @SerializedName("bounds")
    var bounds: RouteBounds
)