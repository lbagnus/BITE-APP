package com.oasis.bite.localdata.database.dao

import androidx.room.*
import com.oasis.bite.localdata.database.entities.LocalReceta

@Dao
interface RecetaDao {

    // 1. Insertar (si ya existe con mismo localId, la reemplaza)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarReceta(receta: LocalReceta)

    // 2. Obtener todas las recetas locales
    @Query("SELECT * FROM recetas ORDER BY localId DESC")
    suspend fun obtenerTodas(): List<LocalReceta>

    // 3. Obtener recetas no sincronizadas con el servidor
    @Query("SELECT * FROM recetas WHERE pendienteDeSync = 1")
    suspend fun obtenerPendientes(): List<LocalReceta>

    // 4. Marcar una receta como sincronizada (después de subirla al server)
    @Query("UPDATE recetas SET pendienteDeSync = 0 WHERE localId = :localId")
    suspend fun marcarComoSincronizada(localId: Int)

    // 5. (Opcional) Eliminar receta local
    @Delete
    suspend fun eliminarReceta(receta: LocalReceta)

    @Query("SELECT COUNT(*) FROM recetas")
    suspend fun contarRecetasLocales(): Int

    // En RecetaDao.kt
    @Query("SELECT * FROM recetas WHERE localId = :localId LIMIT 1")
    suspend fun obtenerPorLocalId(localId: Int): LocalReceta?
}
