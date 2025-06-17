package com.oasis.bite.data.model

import com.google.gson.annotations.SerializedName

data class RecetaSimpleResponse(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val tiempo: String,
    val porciones: Int,
    val dificultad: String,
    val imagen: String?,
    val estado: String,
    val reviewCount: Int?,
    val averageRating: Float?,
    val createdAt: String?,
    val updatedAt: String?,
    val username: String,
    val CategoriumCategoria: String?
)