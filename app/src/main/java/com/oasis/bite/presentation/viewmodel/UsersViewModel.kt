package com.oasis.bite.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oasis.bite.data.RetrofitInstance
import com.oasis.bite.data.repository.RecetaRepository
import com.oasis.bite.domain.models.User

import com.oasis.bite.data.repository.UserRepository
import com.oasis.bite.localdata.database.AppDatabase
import com.oasis.bite.localdata.database.entities.LocalUser
import com.oasis.bite.localdata.repository.UserSessionRepository
import kotlinx.coroutines.launch
import retrofit2.Response

class UsersViewModel(private val sessionRepository: UserSessionRepository) : ViewModel() {

    private val apiService = RetrofitInstance.apiService

    private val repository = UserRepository(apiService)

    private val _usuarioLogueado = MutableLiveData<User?>()
    val usuarioLogueado: LiveData<User?> = _usuarioLogueado

    private val _mensajeError = MutableLiveData<String>()
    val mensajeError: LiveData<String> = _mensajeError

    fun inicializarSesion() {
        viewModelScope.launch {
            val userEntity = sessionRepository.getUserSession()
            if (userEntity != null) {
                _usuarioLogueado.value = User(
                    email = userEntity.email,
                    username = userEntity.username,
                    firstName = userEntity.firstName,
                    lastName = userEntity.lastName,
                    role = userEntity.role
                )
            }
        }
    }


    fun login(email: String, password: String) {
        viewModelScope.launch {
            val usuario = repository.login(email, password)
            if (usuario != null) {
                _usuarioLogueado.value = usuario

                // Guardar usuario en Room
                val userEntity = LocalUser(
                    email = usuario.email,
                    username = usuario.username,
                    firstName = usuario.firstName,
                    lastName = usuario.lastName,
                    role = usuario.role
                )
                sessionRepository.saveUserSession(userEntity)
            } else {
                _mensajeError.value = "Usuario o contraseña incorrectos"
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionRepository.clearSession()
            _usuarioLogueado.value = null
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

    suspend fun resetPassword(email: String, password: String):Response<Unit>{
        return repository.resetContraseña(email, password)
    }
}
