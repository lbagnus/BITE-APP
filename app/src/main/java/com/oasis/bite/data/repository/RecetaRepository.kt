package com.oasis.bite.data.repository

import android.util.Log
import com.oasis.bite.data.api.ApiService
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
}

