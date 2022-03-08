package com.example.licenta.api

import com.example.licenta.api.model.GymPlaces
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface PlaceAPIService {
    @GET
    fun getNearbyGyms(@Url url: String): Call<GymPlaces>
}