package com.oasis.bite.data.repository

import android.util.Log
import com.oasis.bite.data.api.ApiService
import com.oasis.bite.data.model.CodeRequest
import com.oasis.bite.data.model.LoginRequest
import com.oasis.bite.data.model.PassRequest
import com.oasis.bite.data.model.PassResetRequest
import com.oasis.bite.data.toUser
import retrofit2.Response

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

    suspend fun verificar(email: String, code: String): Response<Unit> {
        return apiService.verify(CodeRequest(email, code))
    }

    suspend fun enviar(email: String): Response<Unit> {
        return apiService.sendResetCode(email)
    }

    suspend fun cambiarContraseña(email: String, oldpassword: String, newpassword: String): Response<Unit> {
        return apiService.cambiarPassword(PassRequest(email, oldpassword, newpassword))
    }
    suspend fun resetContraseña(email: String, password: String): Response<Unit> {
        return apiService.resetearPassword(PassResetRequest(email, password))
    }

    suspend fun getWifiPreference(userEmail: String): Boolean {
        return try {
            val response = apiService.getFormatoCarga(userEmail) // Asumo que devuelve Response<WifiPreferenceResponse>
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                Log.e("UserRepository", "Error GET preferencia WiFi: ${response.code()} - ${response.message()}")
                false // Por defecto a false en caso de error o no exitoso
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Excepción GET preferencia WiFi: ${e.message}", e)
            false // Por defecto a false en caso de excepción
        }
    }
    suspend fun cambiarWifiSwitch(userEmail: String): Boolean {
        return try {
            val response = apiService.cambiarFormatoCarga(userEmail)
            if (response.isSuccessful) {
                Log.d("UserRepository", "Preferencia WiFi cambiada en la API exitosamente.")
               response.body()!!
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("UserRepository", "Error al cambiar preferencia WiFi en API: ${response.code()} - ${response.message()}. Cuerpo: $errorBody")
                false
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Excepción al cambiar preferencia WiFi en API: ${e.message}", e)
            false
        }
    }
}