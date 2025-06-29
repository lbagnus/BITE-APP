package com.oasis.bite.data.api

import com.oasis.bite.data.model.CodeRequest
import com.oasis.bite.data.model.CommentRequest
import com.oasis.bite.data.model.FavRequest
import com.oasis.bite.data.model.ImageUploadResponse
import com.oasis.bite.data.model.LoginRequest
import com.oasis.bite.data.model.LoginResponse
import com.oasis.bite.data.model.PassRequest
import com.oasis.bite.data.model.PassResetRequest
import com.oasis.bite.data.model.RecetaDTO
import com.oasis.bite.data.model.RecetaRequest
import com.oasis.bite.data.model.RecetaSimpleResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
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

    @GET("favoritos/{email}")
    suspend fun getRecetaFavoritos(@Path("email") email: String): Response<List<RecetaSimpleResponse>>



    @HTTP(method = "DELETE", path = "favoritos/eliminar", hasBody = true)
    suspend fun deletefav(@Body params: FavRequest): Response<Unit>


    @POST("favoritos/agregar")
    suspend fun addfav(@Body params : FavRequest)

    @POST("users/recover/{email}")
    suspend fun sendResetCode(@Path("email") email: String): Response<Unit>

    @POST("users/verify")
    suspend fun verify(@Body params : CodeRequest): Response<Unit>

    @PUT("users/password")
    suspend fun cambiarPassword(@Body params: PassRequest): Response<Unit>

    @POST("recetas/review")
    suspend fun addComment(@Body params : CommentRequest): Response<Unit>

    @POST("recetas")
    suspend fun addReceta(@Body params: RecetaRequest): Response<Unit>

    @Multipart
    @POST("upload/recipe-image")
    suspend fun uploadRecipeImage(@Part image: MultipartBody.Part): Response<ImageUploadResponse>

    @DELETE("recetas/{id}") // Removed hasBody = false
    suspend fun deleteReceta(@Path("id") id: String): Response<Unit>

    @PUT("users/newPassword")
    suspend fun resetearPassword(@Body params: PassResetRequest): Response<Unit>

    @GET("recetas/home?term={termino}")
    suspend fun getBusquedaBarra(@Path("termino") id: String): Response<List<RecetaSimpleResponse>>
}
