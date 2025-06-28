package com.oasis.bite.data.model

import com.google.gson.annotations.SerializedName
import com.oasis.bite.domain.models.Dificultad
import com.oasis.bite.domain.models.Ingrediente
import com.oasis.bite.domain.models.PasoReceta
import com.oasis.bite.domain.models.RecetaStatus

class RecetaRequest(
    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("descripcion")
    val descripcion: String,

    @SerializedName("tiempo")
    val tiempo: String,

    @SerializedName("porciones")
    val porciones: String,

    @SerializedName("dificultad")
    val dificultad: String,

    @SerializedName("imagen")
    val imagen: String,

    @SerializedName("creadorEmail")
    val creadorEmail: String,

    @SerializedName("ingredientes")
    val ingredientes: List<Ingrediente>,

    @SerializedName("pasos")
    val pasos: List<PasoReceta>,

    @SerializedName("estado")
    val estado: String
)