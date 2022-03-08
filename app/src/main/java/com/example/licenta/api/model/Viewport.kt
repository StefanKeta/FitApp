package com.example.licenta.api.model

import com.google.gson.annotations.SerializedName

data class Viewport(
    @SerializedName("northeast") val northeast: Northeast? = Northeast(),
    @SerializedName("southwest") val southwest: Southwest? = Southwest()
)
