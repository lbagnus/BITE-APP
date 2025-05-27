package com.oasis.bite.data

import com.oasis.bite.data.model.IngredienteDTO
import com.oasis.bite.data.model.LoginResponse
import com.oasis.bite.data.model.PasoRecetaDTO
import com.oasis.bite.data.model.RecetaDTO
import com.oasis.bite.domain.models.Dificultad
import com.oasis.bite.domain.models.Ingrediente
import com.oasis.bite.domain.models.PasoReceta
import com.oasis.bite.domain.models.Receta
import com.oasis.bite.domain.models.RecetaStatus
import com.oasis.bite.domain.models.Role
import com.oasis.bite.domain.models.User

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
        nombre = this.nombre,
        descripcion = this.descripcion,
        tiempo = this.tiempo,
        porciones = this.porciones,
        dificultad = Dificultad.valueOf(this.dificultad?.uppercase() ?: "MEDIA"),
        imagen = this.imagen,
        estado = RecetaStatus.valueOf(this.estado.uppercase()),
        username = this.username,
        categoria = this.categoria?.categoria ?: "Sin categoría",
        pasos = this.pasos.map { it.toPasoReceta() },
        reviewCount = this.reviewCount,
        averageRating = this.averageRating,
        ingredientes = this.ingredientes.map { it.toIngrediente() }
    )
}

fun PasoRecetaDTO.toPasoReceta(): PasoReceta {
    return PasoReceta(
        numeroDePaso = this.numeroDePaso,
        contenido = this.contenido,
        archivoFoto = this.archivoFoto
    )
}

fun IngredienteDTO.toIngrediente(): Ingrediente {
    return Ingrediente(
        nombre = this.nombre,
        cantidad = this.cantidad,
        unidad = this.unidad
    )
}



