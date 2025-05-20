package com.oasis.bite.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.oasis.bite.data.model.User

import com.oasis.bite.data.repository.UserRepository

class UsersViewModel : ViewModel() {

    private val repository = UserRepository();

    private val _usuarioLogueado = MutableLiveData<User?>()
    val usuarioLogueado: LiveData<User?> = _usuarioLogueado

    private val _mensajeError = MutableLiveData<String>()
    val mensajeError: LiveData<String> = _mensajeError

    fun login(email: String, password: String) {
        val usuario = repository.login(email, password)
        if (usuario != null) {
            _usuarioLogueado.value = usuario
        } else {
            _mensajeError.value = "Usuario o contraseña incorrectos"
        }
    }
}
