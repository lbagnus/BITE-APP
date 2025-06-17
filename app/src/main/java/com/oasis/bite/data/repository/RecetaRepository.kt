package com.oasis.bite.data.repository

import android.util.Log
import com.google.gson.Gson
import com.oasis.bite.data.api.ApiService
import com.oasis.bite.data.model.RecetaSearchParams
import com.oasis.bite.data.model.RecetaSimpleResponse
import com.oasis.bite.data.toReceta
import com.oasis.bite.domain.models.Receta

class RecetaRepository(private val apiService: ApiService) {

    suspend fun getRecetaPorId(id: String): Receta? {
        Log.d("RecetasRepository", "Solicitando receta con id: $id")
        val response = apiService.getRecetaPorId(id)
        return if (response.isSuccessful && response.body() != null) {
            Log.d("RecetasRepository", "Receta recibida correctamente")
            response.body()!!.toReceta()
        } else {
            Log.e("RecetasRepository", "Error al obtener receta: ${response.code()} - ${response.message()}")
            null
        }
    }


    suspend fun getRecetasHome(): List<Receta> ?{
        val response = apiService.getRecetasHome()
        return if (response.isSuccessful && response.body() != null) {
            Log.d("RecetasRepository", "Receta recibida correctamente")
            return response.body()!!.map { it.toReceta() }
        } else {
            Log.e("RecetasRepository", "Error al obtener receta: ${response.code()} - ${response.message()}")
            null
        }
    }

    suspend fun getRecetasSearch(params: RecetaSearchParams): List<Receta>? {
        val response = apiService.searchRecetas(
            name = params.name,
            userName = params.userName,
            category = params.type,
            includeIngredients = params.getIncludeIngredientsString(),
            excludeIngredients = params.getExcludeIngredientsString(),
            orderBy = params.orderBy,
            direction = params.direction,
            limit = params.limit,
            offset = params.offset
        )

        return if (response.isSuccessful && response.body() != null) {
            Log.d("RecetasRepository", "Recetas recibidas correctamente: ${response.body()?.size} recetas")
            val gson = Gson()
            val json = gson.toJson(response.body())
            Log.d("API_RESPONSE", json)
            response.body()!!.map { it.toReceta() }
        } else {
            Log.e("RecetasRepository", "Error al obtener recetas: ${response.code()} - ${response.message()}")
            null
        }
    }

}




