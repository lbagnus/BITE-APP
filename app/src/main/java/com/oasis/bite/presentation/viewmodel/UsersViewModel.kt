package com.oasis.bite.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oasis.bite.data.RetrofitInstance
import com.oasis.bite.data.repository.RecetaRepository
import com.oasis.bite.domain.models.User

import com.oasis.bite.data.repository.UserRepository
import kotlinx.coroutines.launch
import retrofit2.Response

class UsersViewModel : ViewModel() {

    private val apiService = RetrofitInstance.apiService

    private val repository = UserRepository(apiService)
    private val recetaRepo = RecetaRepository(apiService)

    private val _usuarioLogueado = MutableLiveData<User?>()
    val usuarioLogueado: LiveData<User?> = _usuarioLogueado

    private val _mensajeError = MutableLiveData<String>()
    val mensajeError: LiveData<String> = _mensajeError

    fun login(email: String, password: String) {
        viewModelScope.launch {
            // val receta = recetaRepo.getRecetaPorId(1)
            val usuario = repository.login(email, password)
            if (usuario != null) {
                _usuarioLogueado.value = usuario
            } else {
                _mensajeError.value = "Usuario o contraseña incorrectos"
            }
        }
    }

    suspend fun verify(email: String, code: String):Response<Unit>{
            return repository.verificar(email, code)
    }

    suspend fun sendResetCode(email: String):Response<Unit>{
        return repository.enviar(email)
    }

    suspend fun updatePassword(email: String, password: String):Response<Unit>{
        return repository.cambiarContraseña(email, password)
    }
}
