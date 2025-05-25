package com.oasis.bite.domain.models

import com.google.gson.annotations.SerializedName

data class PasoReceta(

    @SerializedName("numeroDePaso")
    val numeroDePaso: Int,

    @SerializedName("contenido")
    val contenido: String,

    @SerializedName("archivoFoto")
    val archivoFoto: String?
)
