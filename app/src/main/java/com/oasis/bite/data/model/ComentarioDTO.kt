package com.oasis.bite.data.model
import com.google.gson.annotations.SerializedName

class ComentarioDTO (

    @SerializedName("id")
    val id: Int,

    @SerializedName("titulo")
    val titulo: String,


    @SerializedName("valoracion")
    val valoracion: Int,

    @SerializedName("reseña")
    val reseña: String,

    @SerializedName("Usuario")
    val username: UsuarioComentarioResponse
)


data class UsuarioComentarioResponse(
    val username: String
)



