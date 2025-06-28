package com.oasis.bite.data.repository

import android.util.Log
import com.google.gson.Gson
import com.oasis.bite.data.api.ApiService
import com.oasis.bite.data.model.CommentRequest
import com.oasis.bite.data.model.FavParams
import com.oasis.bite.data.model.FavRequest
import com.oasis.bite.data.model.RecetaRequest
import com.oasis.bite.data.model.RecetaSearchParams
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

     suspend fun getRecetasFavoritos(email: String): List<Receta>? {
        val response= apiService.getRecetaFavoritos(email)
        return if (response.isSuccessful && response.body() != null) {
            val gson = Gson()
            val json = gson.toJson(response.body())
            Log.d("RecetasRepositoryFavoritos", "Receta recibida correctamente favoritos")
            Log.d("favoritos", json)
            return response.body()!!.map { it.toReceta() }
        } else {
            Log.e("RecetasRepository", "Error al obtener receta: ${response.code()} - ${response.message()}")
            null
        }
    }

    suspend fun deleteFavorito(params: FavParams){
        apiService.deletefav(FavRequest(params.email.toString(),params.recetaId))
    }

    suspend fun addFavorito(params: FavParams){
        apiService.addfav(FavRequest(params.email.toString(),params.recetaId))
    }

    suspend fun addComentario(params: CommentRequest): Int{
        val response = apiService.addComment(CommentRequest(params.titulo, params.reseña,
            params.valoracion, params.usuarioEmail, params.recetaId))
        if (response.isSuccessful){
            return 1
        }else{
            0
        }

        return 0
    }

    suspend fun addReceta(params: RecetaRequest){
        val response = apiService.addReceta(RecetaRequest(
            params.nombre,
            params.descripcion,
            params.tiempo,
            params.porciones,
            params.dificultad,
            params.imagen,
            params.creadorEmail,
            params.ingredientes,
            params.pasos,
            params.estado))
        /*Log.d("RecetaRepository", "Response code: ${response.code()}")
        Log.d("RecetaRepository", "Response successful: ${response.isSuccessful}")
        Log.d("RecetaRepository", "Response body: ${response.body()}")
        Log.d("RecetaRepository", "Response error: ${response.errorBody()?.string()}")

        return if (response.isSuccessful && response.body() != null) {
            val recetaResponse = response.body()!!
            Log.d("RecetaRepository", "LoginResponse: $recetaResponse")
            try {
                Log.d("RecetaRepository", "receta cargada con exito")
                return recetaResponse

            } catch (e: Exception) {
                Log.e("RecetaRepository", "ERROR en agregarrecta: ${e.message}", e)
                Log.e("RecetaRepository", "Stack trace completo: ${e.stackTrace.contentToString()}")
               null
            }
        } else {
            Log.e("RecetaRepository", "Response no exitosa o body null")
           null
        }*/
    }


}




