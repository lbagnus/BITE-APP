package com.oasis.bite.data

import android.util.Log
import com.oasis.bite.data.model.CategoriaDTO
import com.oasis.bite.data.model.ComentarioDTO
import com.oasis.bite.data.model.ComentarioResponse
import com.oasis.bite.data.model.IngredienteDTO
import com.oasis.bite.data.model.LoginResponse
import com.oasis.bite.data.model.PasoRecetaDTO
import com.oasis.bite.data.model.RecetaDTO
import com.oasis.bite.data.model.RecetaSimpleResponse
import com.oasis.bite.domain.models.Category
import com.oasis.bite.domain.models.Comentario
import com.oasis.bite.domain.models.Dificultad
import com.oasis.bite.domain.models.Ingrediente
import com.oasis.bite.domain.models.MediaItem
import com.oasis.bite.domain.models.MediaType
import com.oasis.bite.domain.models.PasoReceta
import com.oasis.bite.domain.models.Receta
import com.oasis.bite.domain.models.RecetaStatus
import com.oasis.bite.domain.models.Role
import com.oasis.bite.domain.models.User
import java.util.UUID
import kotlin.Int

fun LoginResponse.toUser(): User {
    return User(
        email = this.email,
        firstName = this.nombre,
        lastName = this.apellido,
        username = this.username,
        role = Role.valueOf(this.role)
    )
}

fun RecetaDTO.toReceta(): Receta {
    return Receta(
        id = this.id,
        localId = 0,
        nombre = this.nombre,
        descripcion = this.descripcion,
        tiempo = this.tiempo,
        porciones = this.porciones,
        dificultad = Dificultad.valueOf(this.dificultad?.uppercase() ?: "MEDIA"),
        imagen = this.imagen,
        estado = RecetaStatus.valueOf(this.estado.uppercase()),
        username = this.username.username ,
        categoria = this.categoria,
        pasos = this.pasos?.map { it.toPasoReceta() } ?: emptyList(),
        reviewCount = this.reviewCount?: 0,
        averageRating = this.averageRating?: 0f,
        ingredientes = this.ingredientes?.map { it.toIngrediente() } ?: emptyList(),
        comentarios = this.comentarios?.map {it.toComentario()} ?: emptyList(),
        imagenes = this.imagenes?.map{it.toMedia()} ?: emptyList()
    )
}

fun PasoRecetaDTO.toPasoReceta(): PasoReceta {
    return PasoReceta(
        numeroDePaso = this.numeroDePaso.toString(),
        contenido = this.contenido,
        archivoFoto = this.archivoFoto,
        imagenesPasos = this.imagenes?.map{it.toMedia()} ?: emptyList()
    )
}

fun IngredienteDTO.toIngrediente(): Ingrediente {
    return Ingrediente(
        nombre = this.nombre,
        cantidad = this.cantidad,
        unidad = this.unidad
    )
}

fun ComentarioDTO.toComentario(): Comentario {
    return Comentario(
        id = this.id,
        titulo = this.titulo,
        valoracion = this.valoracion,
        reseña = this.reseña,
        userName = this.username.username
    )
}

fun RecetaSimpleResponse.toReceta(): Receta{
    return Receta(
        id = this.id,
        localId = 0,
        nombre = this.nombre,
        descripcion = this.descripcion,
        tiempo = this.tiempo,
        porciones = this.porciones,
        dificultad = Dificultad.valueOf(this.dificultad?.uppercase() ?: "MEDIA"),
        imagen = this.imagen,
        estado = RecetaStatus.valueOf(this.estado.uppercase()),
        username = this.Usuario.username,
        categoria = this.CategoriumCategoria?: "Sin categoría",
        pasos = emptyList(),
        reviewCount = this.reviewCount?:0,
        averageRating = this.averageRating?:0f,
        ingredientes = emptyList(),
        comentarios = emptyList(),
        imagenes = emptyList()
    )
}

fun getMediaTypeFromUrl(url: String): MediaType {
    return when {
        url.endsWith(".mp4", true) || url.contains("video") -> MediaType.VIDEO
        else -> MediaType.IMAGE
    }
}

fun String.toMedia(): MediaItem{
    return MediaItem(
        url = this,
        type = getMediaTypeFromUrl(this)
    )
}

fun ComentarioResponse.toComentario(): Comentario {
    return Comentario(
        id = this.id,
        titulo = this.titulo,
        valoracion = this.valoracion,
        reseña = this.reseña,
        userName = this.Usuario.username
    )
}




