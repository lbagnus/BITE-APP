package com.oasis.bite.data.model

import com.google.gson.annotations.SerializedName

data class RecetaDTO(
    @SerializedName("id")
    val id: Int,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("descripcion")
    val descripcion: String,

    @SerializedName("tiempo")
    val tiempo: String,

    @SerializedName("porciones")
    val porciones: Int,

    @SerializedName("dificultad")
    val dificultad: String,

    @SerializedName("imagen")
    val imagen: String?,

    @SerializedName("estado")
    val estado: String,

    @SerializedName("creadorEmail")
    val creadorEmail: String,

    @SerializedName("Categorium")
    val categoria: CategoriaDTO?,

    @SerializedName("IngredientesReceta")
    val ingredientes: List<IngredienteDTO>,

    @SerializedName("PasoReceta")
    val pasos: List<PasoRecetaDTO>
)

