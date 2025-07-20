package com.oasis.bite.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oasis.bite.data.RetrofitInstance
import com.oasis.bite.data.repository.RecetaRepository
import com.oasis.bite.domain.models.User

import com.oasis.bite.data.repository.UserRepository
import com.oasis.bite.domain.models.Role
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

    private val _localSessionExists = MutableLiveData<Boolean>()
    val localSessionExists: LiveData<Boolean> = _localSessionExists

    private val _wifiPreference = MutableLiveData<Boolean>()
    val wifiPreference: LiveData<Boolean> get() = _wifiPreference

    // LiveData para el estado de la operación (opcional, pero útil para feedback)
    private val _preferenceUpdateStatus = MutableLiveData<Boolean>()
    val preferenceUpdateStatus: LiveData<Boolean> get() = _preferenceUpdateStatus


    init {
        // Llama a inicializarSesion tan pronto como el ViewModel se crea
        // para que el estado de la sesión local esté disponible desde el principio.
        inicializarSesion()
    }

    fun inicializarSesion() {
        viewModelScope.launch {
            val userEntity = sessionRepository.getUserSession()
            if (userEntity != null) {
                _usuarioLogueado.value = User(
                    email = userEntity.email,
                    username = userEntity.username,
                    firstname = userEntity.firstName,
                    lastname = userEntity.lastName,
                    role = userEntity.role
                )
                _localSessionExists.value = true // Sesión local encontrada
                Log.d("UsersViewModel", "Sesión local encontrada para: ${userEntity.username}")
            } else {
                _usuarioLogueado.value = null
                _localSessionExists.value = false // No se encontró sesión local
                Log.d("UsersViewModel", "No se encontró sesión local.")
            }
        }
    }


    fun login(email: String, password: String) {
        viewModelScope.launch {
            try { // Agregamos un try-catch para manejar errores de red/API
                val usuario = repository.login(email, password)
                if (usuario != null) {
                    _usuarioLogueado.value = usuario

                    // Guardar usuario en Room
                    val userEntity = LocalUser(
                        email = usuario.email,
                        username = usuario.username,
                        firstName = usuario.firstname,
                        lastName = usuario.firstname,
                        role = usuario.role
                    )
                    if(usuario.role != Role.GUEST){
                    sessionRepository.saveUserSession(userEntity)
                    _localSessionExists.value = true} // Actualizar estado de sesión local después de login exitoso
                } else {
                    _mensajeError.value = "Usuario o contraseña incorrectos"
                    _usuarioLogueado.value = null
                    _localSessionExists.value = false // Login fallido
                }
            } catch (e: Exception) {
                _mensajeError.value = "Error de red o servidor: ${e.message}"
                _usuarioLogueado.value = null
                _localSessionExists.value = false // Error de login
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionRepository.clearSession()
            _usuarioLogueado.value = null
            _localSessionExists.value = false // Sesión local borrada
        }
    }

    suspend fun verify(email: String, code: String):Response<Unit>{
            return repository.verificar(email, code)
    }

    suspend fun sendResetCode(email: String):Response<Unit>{
        return repository.enviar(email)
    }

    suspend fun updatePassword(email: String, oldpassword: String, newpassword: String):Response<Unit>{
        return repository.cambiarContraseña(email, oldpassword, newpassword)
    }

    suspend fun resetPassword(email: String, password: String):Response<Unit>{
        return repository.resetContraseña(email, password)
    }

    fun loadWifiPreference(userEmail: String) {
        viewModelScope.launch {
            try {
                // Asumo que tu UserRepository tiene un método para obtener la preferencia del switch
                val preference = repository.getWifiPreference(userEmail)
                _wifiPreference.postValue(preference)
                Log.d("UsersViewModel", "Preferencia WiFi cargada: $preference")
            } catch (e: Exception) {
                Log.e("UsersViewModel", "Error al cargar preferencia WiFi: ${e.message}", e)
                // Podrías poner un valor por defecto o manejar el error en la UI
                _wifiPreference.postValue(false) // Por defecto a false en caso de error
                _preferenceUpdateStatus.postValue(false) // Indica que la carga falló
            }
        }
    }

    // Método para cambiar la preferencia del switch en la API
    fun toggleWifiPreference(userEmail: String) { // No necesitas pasar el valor, la API lo invierte
        viewModelScope.launch {
            try {
                // Asumo que tu UserRepository.toggleWifiPreference ya invierte el valor en la API
                val success = repository.cambiarWifiSwitch(userEmail)
                if (success) {
                    // Si la API responde con éxito, actualiza el LiveData local invirtiendo el valor actual
                    _wifiPreference.value = !(_wifiPreference.value ?: false) // Invierte el valor actual, si es null usa false
                    _preferenceUpdateStatus.postValue(true) // Notifica éxito
                    Log.d("UsersViewModel", "Preferencia WiFi cambiada exitosamente. Nuevo valor: ${_wifiPreference.value}")
                } else {
                    Log.e("UsersViewModel", "Fallo al cambiar preferencia WiFi en la API.")
                    _preferenceUpdateStatus.postValue(false) // Notifica fallo
                }
            } catch (e: Exception) {
                Log.e("UsersViewModel", "Excepción al cambiar preferencia WiFi: ${e.message}", e)
                _preferenceUpdateStatus.postValue(false) // Notifica fallo por excepción
            }
        }
    }


}
