package com.example.veoappchallenge.model.parsing

import com.google.gson.annotations.SerializedName

data class DistanceLeg(
    @SerializedName("text")
    var distance: String,
    @SerializedName("value")
    var value: Int
)
