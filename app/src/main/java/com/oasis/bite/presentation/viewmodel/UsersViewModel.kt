package com.oasis.bite.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oasis.bite.data.RetrofitInstance
import com.oasis.bite.data.model.RecetaSearchParams
import com.oasis.bite.data.model.RecetaSimpleResponse
import com.oasis.bite.data.repository.RecetasRepository
import com.oasis.bite.domain.models.User

import com.oasis.bite.data.repository.UserRepository
import kotlinx.coroutines.launch

class UsersViewModel : ViewModel() {

    private val apiService = RetrofitInstance.apiService

    private val repository = UserRepository(apiService)
    private val recetaRepo = RecetasRepository(apiService)

    private val _usuarioLogueado = MutableLiveData<User?>()
    val usuarioLogueado: LiveData<User?> = _usuarioLogueado

    private val _mensajeError = MutableLiveData<String>()
    val mensajeError: LiveData<String> = _mensajeError

    fun login(email: String, password: String) {
        viewModelScope.launch {
            //val receta = recetaRepo.getRecetasHome();

            val params = RecetaSearchParams(name = "Bondiola");
            val recetas = recetaRepo.getRecetasSearch(params);

            val usuario = repository.login(email, password)
            if (usuario != null) {
                _usuarioLogueado.value = usuario
            } else {
                _mensajeError.value = "Usuario o contraseña incorrectos"
            }
        }
    }
}
