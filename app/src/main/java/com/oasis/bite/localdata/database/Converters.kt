package com.oasis.bite.localdata.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.oasis.bite.data.model.PasoRecetaRequest
import com.oasis.bite.domain.models.Ingrediente
import com.oasis.bite.domain.models.PasoReceta

class Converters {

    private val gson = Gson()

    @TypeConverter
    fun fromIngredientes(ingredientes: List<Ingrediente>): String {
        return gson.toJson(ingredientes)
    }

    @TypeConverter
    fun toIngredientes(data: String): List<Ingrediente> {
        val type = object : TypeToken<List<Ingrediente>>() {}.type
        return gson.fromJson(data, type)
    }

    @TypeConverter
    fun fromPasos(pasos: List<PasoRecetaRequest>): String {
        return gson.toJson(pasos)
    }

    @TypeConverter
    fun toPasos(data: String): List<PasoReceta> {
        val type = object : TypeToken<List<PasoRecetaRequest>>() {}.type
        return gson.fromJson(data, type)
    }
}
