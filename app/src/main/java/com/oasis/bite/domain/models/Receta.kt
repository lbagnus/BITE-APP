package com.oasis.bite.domain.models

import com.google.gson.annotations.SerializedName

data class Receta(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val tiempo: String?,
    val porciones: Int,
    val dificultad: Dificultad,
    val imagen: String?,
    val estado: RecetaStatus,
    val username: String,
    val categoria: String,
    val reviewCount: Int,
    val averageRating: Float?,
    val pasos: List<PasoReceta>,
    val ingredientes: List<Ingrediente>
)

