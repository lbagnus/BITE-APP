package com.oasis.bite.data.repository

import android.util.Log
import com.oasis.bite.data.api.ApiService
import com.oasis.bite.data.model.RecetaSearchParams
import com.oasis.bite.data.model.RecetaSimpleResponse
import com.oasis.bite.data.toReceta
import com.oasis.bite.domain.models.Receta

class RecetasRepository(private val apiService: ApiService) {

    suspend fun getRecetaPorId(id: Int): Receta? {
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

    suspend fun getRecetasHome(): List<RecetaSimpleResponse> ?{
        val response = apiService.getRecetasHome()
        return if (response.isSuccessful && response.body() != null) {
            Log.d("RecetasRepository", "Receta recibida correctamente")
            return response.body()
        } else {
            Log.e("RecetasRepository", "Error al obtener receta: ${response.code()} - ${response.message()}")
            null
        }
    }

    suspend fun getRecetasSearch(params: RecetaSearchParams): List<RecetaSimpleResponse>? {
        val response = apiService.searchRecetas(
            name = params.name,
            userName = params.userName,
            type = params.type,
            includeIngredients = params.getIncludeIngredientsString(),
            excludeIngredients = params.getExcludeIngredientsString(),
            orderBy = params.orderBy,
            direction = params.direction,
            limit = params.limit,
            offset = params.offset
        )

        return if (response.isSuccessful && response.body() != null) {
            Log.d("RecetasRepository", "Recetas recibidas correctamente: ${response.body()?.size} recetas")
            response.body()
        } else {
            Log.e("RecetasRepository", "Error al obtener recetas: ${response.code()} - ${response.message()}")
            null
        }
    }
}





