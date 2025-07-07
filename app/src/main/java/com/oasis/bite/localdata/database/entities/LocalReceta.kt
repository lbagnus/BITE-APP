package com.oasis.bite.localdata.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recetas")
data class LocalReceta(
    @PrimaryKey(autoGenerate = true)
    val localId: Int = 0, // ID local, se usa si no hay conexión
    val idRemoto: Int?, // ID del servidor (puede ser null si aún no se sincronizó)
    val nombre: String,
    val descripcion: String,
    val tiempo: String?,
    val porciones: String,
    val dificultad: String, // guardamos como String por simplicidad
    val imagen: String?,
    val imagenes: String,
    val username: String,
    val categoria: String,
    val pasosJson: String, // serializado
    val ingredientesJson: String, // serializado
    val pendienteDeSync: Boolean = true
)
