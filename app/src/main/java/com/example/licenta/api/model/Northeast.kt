package com.example.licenta.api.model

import com.google.gson.annotations.SerializedName

data class Northeast(
    @SerializedName("lat") val lat: Double? = null,
    @SerializedName("lng") val lng: Double? = null
)
