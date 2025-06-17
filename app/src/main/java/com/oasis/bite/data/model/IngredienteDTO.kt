package com.oasis.bite.data.model

import com.google.gson.annotations.SerializedName

data class IngredienteDTO(
    @SerializedName("id")
    val id: Int,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("cantidad")
    val cantidad: Float,

    @SerializedName("unidad")
    val unidad: String,

    @SerializedName("seleccionado")
    var seleccionado: Boolean = false
)
