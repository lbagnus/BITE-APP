package com.oasis.bite.data.repository

import android.util.Log
import com.oasis.bite.data.api.ApiService
import com.oasis.bite.data.model.LoginRequest
import com.oasis.bite.data.toUser

class UserRepository(private val apiService: ApiService) {

    suspend fun login(email: String, password: String): com.oasis.bite.domain.models.User? {
        val response = apiService.loginUser(LoginRequest(email, password))

        Log.d("UserRepository", "Response code: ${response.code()}")
        Log.d("UserRepository", "Response successful: ${response.isSuccessful}")
        Log.d("UserRepository", "Response body: ${response.body()}")
        Log.d("UserRepository", "Response error: ${response.errorBody()?.string()}")

        return if (response.isSuccessful && response.body() != null) {
            val loginResponse = response.body()!!
            Log.d("UserRepository", "LoginResponse: $loginResponse")

            try {
                val user = loginResponse.toUser()
                Log.d("UserRepository", "User convertido: $user")
                user
            } catch (e: Exception) {
                Log.e("UserRepository", "ERROR en toUser(): ${e.message}", e)
                Log.e("UserRepository", "Stack trace completo: ${e.stackTrace.contentToString()}")
                null // o lanza la excepción si prefieres
            }
        } else {
            Log.e("UserRepository", "Response no exitosa o body null")
            null
        }
    }
}
