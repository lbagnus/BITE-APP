package com.oasis.bite.domain.models

import com.google.gson.annotations.SerializedName

data class PasoReceta(

    @SerializedName("numeroDePaso")
    val numeroDePaso: String,

    @SerializedName("contenido")
    val contenido: String,

    @SerializedName("archivoFoto")
    val archivoFoto: String?,

    @SerializedName("imagenes")
    val imagenesPasos: List<MediaItem>?
)
