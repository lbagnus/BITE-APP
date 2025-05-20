package com.oasis.bite.data.repository

import com.oasis.bite.data.model.User

class UserRepository {

    // Lista simulada de usuarios
    private val usuarios = listOf(
        User("1", "thomas", "thomas@email.com", "1234", "admin"),
        User("2", "laura", "laura@email.com", "5678", "user"),
        User("3", "eduardo", "edu@email.com", "abcd", "user")
    )

    // Simula el login buscando por username y password
    fun login(email: String, password: String): User? {
        return usuarios.find { it.email == email && it.password == password }
    }

    // Buscar un usuario por ID
    fun getUsuarioById(id: String): User? {
        return usuarios.find { it.id == id }
    }

    // Buscar por email
    fun getUsuarioByEmail(email: String): User? {
        return usuarios.find { it.email == email }
    }
}
