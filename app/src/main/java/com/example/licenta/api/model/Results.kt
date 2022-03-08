package com.example.licenta.api.model


import com.google.gson.annotations.SerializedName


data class Results(

    @SerializedName("business_status") val businessStatus: String? = null,
    @SerializedName("geometry") val geometry: Geometry? = Geometry(),
    @SerializedName("icon") val icon: String? = null,
    @SerializedName("icon_background_color") val iconBackgroundColor: String? = null,
    @SerializedName("icon_mask_base_uri") val iconMaskBaseUri: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("opening_hours") val openingHours: OpeningHours? = OpeningHours(),
    @SerializedName("photos") val photos: ArrayList<Photos> = arrayListOf(),
    @SerializedName("place_id") val placeId: String? = null,
    @SerializedName("plus_code") val plusCode: PlusCode? = PlusCode(),
    @SerializedName("rating") val rating: Double? = null,
    @SerializedName("reference") val reference: String? = null,
    @SerializedName("scope") val scope: String? = null,
    @SerializedName("types") val types: ArrayList<String> = arrayListOf(),
    @SerializedName("user_ratings_total") val userRatingsTotal: Int? = null,
    @SerializedName("vicinity") val vicinity: String? = null

)