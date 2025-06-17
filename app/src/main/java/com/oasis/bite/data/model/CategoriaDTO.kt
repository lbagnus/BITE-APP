package com.oasis.bite.data.model

import com.google.gson.annotations.SerializedName

data class CategoriaDTO(
    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("iconoResId")
    val iconoResId: Int,

)

