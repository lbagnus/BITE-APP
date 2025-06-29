package com.oasis.bite.localdata.database.dao

import androidx.room.*
import com.oasis.bite.localdata.database.entities.LocalUser

/**
 * DAO para operaciones de Usuario
 */

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: LocalUser)

    @Query("SELECT * FROM user_session LIMIT 1")
    suspend fun getLoggedUser(): LocalUser?

    @Query("DELETE FROM user_session")
    suspend fun clearSession()
}
