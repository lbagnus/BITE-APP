package com.oasis.bite.data.model

import com.google.gson.annotations.SerializedName

data class ComentarioResponse (
    val id: Int,
    val titulo: String,
    val valoracion: Int,
    val reseña: String,
    val estado: String,
    val Usuario: UsuarioComentarioResponse2,
    val RecetumId: Int
)
data class UsuarioComentarioResponse2(
    val username: String
)