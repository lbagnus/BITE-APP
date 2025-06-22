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
    val username: String,
    val categoria: String,
    val reviewCount: Int,
    val averageRating: Float,
    val pasos: List<PasoReceta>,
    val ingredientes: List<Ingrediente>,
    val comentarios: List<Comentario>?,
    val imagenes: List<MediaItem>?
)

data class MediaItem(
    val url: String,
    val type: MediaType // IMAGE o VIDEO
)

enum class MediaType {
    IMAGE,
    VIDEO
}

