package com.oasis.bite.data.model

import com.google.gson.annotations.SerializedName

class FavRequest (
    @SerializedName("email")
    val email: String,

    @SerializedName("recetaId")
    val recetaId: Int?
)