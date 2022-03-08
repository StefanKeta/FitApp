package com.example.licenta.api.model

import com.google.gson.annotations.SerializedName

data class Geometry(
    @SerializedName("location") val location: Location = Location(),
    @SerializedName("viewport") val viewport: Viewport = Viewport()
)
