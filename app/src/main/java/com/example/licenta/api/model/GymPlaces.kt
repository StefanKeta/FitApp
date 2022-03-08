package com.example.licenta.api.model

import com.google.gson.annotations.SerializedName

data class GymPlaces(
    @SerializedName("html_attributions") val htmlAttributions: ArrayList<String> = arrayListOf(),
    @SerializedName("next_page_token") val nextPageToken: String? = null,
    @SerializedName("results") val results: ArrayList<Results> = arrayListOf(),
    @SerializedName("status") val status: String? = null
)
