package com.example.licenta.api.model

import com.google.gson.annotations.SerializedName

data class Photos(
    @SerializedName("height") val height: Int? = null,
    @SerializedName("html_attributions") val htmlAttributions: ArrayList<String> = arrayListOf(),
    @SerializedName("photo_reference") val photoReference: String? = null,
    @SerializedName("width") val width: Int? = null
)
