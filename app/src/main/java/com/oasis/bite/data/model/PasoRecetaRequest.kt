package com.oasis.bite.data.model

import com.google.gson.annotations.SerializedName
import com.oasis.bite.domain.models.MediaItem

data class PasoRecetaRequest (

    @SerializedName("numeroDePaso")
    val numeroDePaso: String,

    @SerializedName("contenido")
    val contenido: String,
    @SerializedName("archivoFoto")
    val archivoFoto: String?,

    @SerializedName("imagenes")
    val imagenesPasos: List<String>?
)