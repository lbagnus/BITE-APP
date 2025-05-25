package com.oasis.bite.domain.models

import com.google.gson.annotations.SerializedName

data class Ingrediente(

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("cantidad")
    val cantidad: Int,

    @SerializedName("unidad")
    val unidad: String
)
