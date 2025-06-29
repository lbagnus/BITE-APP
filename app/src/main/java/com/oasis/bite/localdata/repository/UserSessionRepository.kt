package com.oasis.bite.localdata.repository

import android.content.Context
import com.oasis.bite.localdata.database.dao.UserDao
import com.oasis.bite.localdata.database.entities.LocalUser
import com.oasis.bite.localdata.database.DatabaseProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserSessionRepository private constructor(private val userDao: UserDao) {

    // Guardar sesión de usuario
    suspend fun saveUserSession(user: LocalUser) {
        withContext(Dispatchers.IO) {
            userDao.insertUser(user)
        }
    }

    // Obtener sesión activa, si existe
    suspend fun getUserSession(): LocalUser? {
        return withContext(Dispatchers.IO) {
            userDao.getLoggedUser()
        }
    }

    // Cerrar sesión
    suspend fun clearSession() {
        withContext(Dispatchers.IO) {
            userDao.clearSession()
        }
    }

    companion object {
        @Volatile private var INSTANCE: UserSessionRepository? = null

        fun getInstance(context: Context): UserSessionRepository {
            return INSTANCE ?: synchronized(this) {
                val db = DatabaseProvider.getDatabase(context)
                val instance = UserSessionRepository(db.userDao())
                INSTANCE = instance
                instance
            }
        }
    }
}