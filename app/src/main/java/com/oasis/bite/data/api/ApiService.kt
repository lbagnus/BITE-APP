package com.oasis.bite.data.api

import com.oasis.bite.data.model.CategoriaDTO
import com.oasis.bite.data.model.LoginRequest
import com.oasis.bite.data.model.LoginResponse
import com.oasis.bite.data.model.RecetaDTO
import com.oasis.bite.data.model.RecetaSimpleResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("users/login")
    suspend fun loginUser(@Body credentials: LoginRequest): Response<LoginResponse>

    @GET("recetas/{id}")
    suspend fun getRecetaPorId(@Path("id") id: String): Response<RecetaDTO>


    @GET("recetas/search?orderBy=newest&direction=desc&limit=10")
    suspend fun getRecetasHome(): Response<List<RecetaSimpleResponse>>

    // Endpoint para buscar recetas con filtros
    @GET("recetas/search")
    suspend fun searchRecetas(
        @Query("name") name: String? = null,
        @Query("userName") userName: String? = null,
        @Query("category") category: String? = null,
        @Query("includeIngredients") includeIngredients: String? = null, // Comma-separated string
        @Query("excludeIngredients") excludeIngredients: String? = null, // Comma-separated string
        @Query("orderBy") orderBy: String = "newest",
        @Query("direction") direction: String = "asc",
        @Query("limit") limit: Int = 10,
        @Query("offset") offset: Int = 0
    ): Response<List<RecetaSimpleResponse>>
}
