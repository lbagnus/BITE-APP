package com.oasis.bite.domain.models

data class Receta(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val tiempo: String?,
    val porciones: Int,
    val dificultad: Dificultad,
    val imagen: String?,
    val estado: RecetaStatus,
    val creador: User, // solo si querés mostrar nombre/email del creador
    val categoria: String,
    val pasos: List<PasoReceta>,
    val ingredientes: List<Ingrediente>
)

