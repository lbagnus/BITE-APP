package com.oasis.bite.data.model

import com.google.gson.annotations.SerializedName

class CommentRequest (
    @SerializedName("titulo")
    val titulo: String,

    @SerializedName("reseña")
    val reseña: String,

    @SerializedName("valoracion")
    val valoracion: String,

    @SerializedName("usuarioEmail")
    val usuarioEmail: String,

    @SerializedName("recetaId")
    val recetaId: Float
)