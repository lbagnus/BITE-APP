package com.oasis.bite.data.model

import com.google.gson.annotations.SerializedName

data class PasoRecetaDTO(
    @SerializedName("id")
    val id: Int,

    @SerializedName("numeroDePaso")
    val numeroDePaso: Int,

    @SerializedName("contenido")
    val contenido: String,

    @SerializedName("archivoFoto")
    val archivoFoto: String?,

    @SerializedName("imagenes")
    val imagenes: List<String>?
)

