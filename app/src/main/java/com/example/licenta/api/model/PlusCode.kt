package com.example.licenta.api.model

import com.google.gson.annotations.SerializedName


data class PlusCode(

    @SerializedName("compound_code") val compoundCode: String? = null,
    @SerializedName("global_code") val globalCode: String? = null

)