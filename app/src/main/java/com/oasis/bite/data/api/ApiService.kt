package com.oasis.bite.data.api

import com.oasis.bite.data.model.LoginRequest
import com.oasis.bite.data.model.LoginResponse
import com.oasis.bite.data.model.RecetaDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("users/login")
    suspend fun loginUser(@Body credentials: LoginRequest): Response<LoginResponse>

    @GET("recetas/{id}")
    suspend fun getRecetaPorId(@Path("id") id: Int): Response<RecetaDTO>

}
