package com.oasis.bite.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.oasis.bite.data.api.ApiService
import com.oasis.bite.data.model.CommentRequest
import com.oasis.bite.data.model.EstadoRequest
import com.oasis.bite.data.model.FavParams
import com.oasis.bite.data.model.FavRequest
import com.oasis.bite.data.model.PasoRecetaRequest
import com.oasis.bite.data.model.RecetaRequest
import com.oasis.bite.data.model.RecetaSearchParams
import com.oasis.bite.data.toComentario
import com.oasis.bite.data.toReceta
import com.oasis.bite.domain.models.Comentario
import com.oasis.bite.domain.models.Dificultad
import com.oasis.bite.domain.models.Ingrediente
import com.oasis.bite.domain.models.MediaItem
import com.oasis.bite.domain.models.MediaType
import com.oasis.bite.domain.models.PasoReceta
import com.oasis.bite.domain.models.Receta
import com.oasis.bite.domain.models.RecetaStatus
import com.oasis.bite.localdata.database.dao.RecetaDao
import com.oasis.bite.localdata.database.entities.LocalReceta
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class RecetaRepository(
    private val apiService: ApiService,
    private val recetaDao: RecetaDao,
    private val context: Context
) {
    private val MAX_LOCAL_ADJUSTED_RECIPES = 10
    suspend fun getRecetaPorId(id: String): Receta? {
        Log.d("RecetasRepository", "Solicitando receta con id: $id")
        val response = apiService.getRecetaPorId(id)
        return if (response.isSuccessful && response.body() != null) {
            Log.d("RecetasRepository", "Receta recibida correctamente")
            response.body()!!.toReceta()
        } else {
            Log.e("RecetasRepository", "Error al obtener receta: ${response.code()} - ${response.message()}")
            null
        }
    }

    suspend fun getBySearch(termino: String): List<Receta> ?{
        val response = apiService.getBusquedaBarra(termino)
        return if (response.isSuccessful && response.body() != null) {
            Log.d("RecetasRepository", "Receta recibida correctamente")
            return response.body()!!.map { it.toReceta() }
        } else {
            Log.e("RecetasRepository", "Error al obtener receta: ${response.code()} - ${response.message()}")
            null
        }
    }

    suspend fun getRecetasUsuario(email: String): List<Receta> ?{
        val response = apiService.getRecetasUsuarios(email)
        return if (response.isSuccessful && response.body() != null) {
            Log.d("RecetasRepository", "Receta recibida correctamente")
            return response.body()!!.map { it.toReceta() }
        } else {
            Log.e("RecetasRepository", "Error al obtener receta: ${response.code()} - ${response.message()}")
            null
        }
    }

    suspend fun getRecetasHome(): List<Receta> ?{
        val response = apiService.getRecetasHome()
        return if (response.isSuccessful && response.body() != null) {
            Log.d("RecetasRepository", "Receta recibida correctamente")
            return response.body()!!.map { it.toReceta() }
        } else {
            Log.e("RecetasRepository", "Error al obtener receta: ${response.code()} - ${response.message()}")
            null
        }
    }

    suspend fun getRecetasSearch(params: RecetaSearchParams): List<Receta>? {
        val response = apiService.searchRecetas(
            name = params.name,
            userName = params.userName,
            category = params.type,
            includeIngredients = params.getIncludeIngredientsString(),
            excludeIngredients = params.getExcludeIngredientsString(),
            orderBy = params.orderBy,
            direction = params.direction,
            limit = params.limit,
            offset = params.offset
        )

        return if (response.isSuccessful && response.body() != null) {
            Log.d("RecetasRepository", "Recetas recibidas correctamente: ${response.body()?.size} recetas")
            val gson = Gson()
            val json = gson.toJson(response.body())
            Log.d("API_RESPONSE", json)
            response.body()!!.map { it.toReceta() }
        } else {
            Log.e("RecetasRepository", "Error al obtener recetas: ${response.code()} - ${response.message()}")
            null
        }
    }

     suspend fun getRecetasFavoritos(email: String): List<Receta>? {
        val response= apiService.getRecetaFavoritos(email)
        return if (response.isSuccessful && response.body() != null) {
            val gson = Gson()
            val json = gson.toJson(response.body())
            Log.d("RecetasRepositoryFavoritos", "Receta recibida correctamente favoritos")
            Log.d("favoritos", json)
            return response.body()!!.map { it.toReceta() }
        } else {
            Log.e("RecetasRepository", "Error al obtener receta: ${response.code()} - ${response.message()}")
            null
        }
    }

    suspend fun deleteFavorito(params: FavParams){
        apiService.deletefav(FavRequest(params.email.toString(),params.recetaId))
    }

    suspend fun addFavorito(params: FavParams){
        apiService.addfav(FavRequest(params.email.toString(),params.recetaId))
    }

    suspend fun addComentario(params: CommentRequest): Int{
        val response = apiService.addComment(CommentRequest(params.titulo, params.reseña,
            params.valoracion, params.usuarioEmail, params.recetaId))
        if (response.isSuccessful){
            return 1
        }else{
            0
        }

        return 0
    }

    // `addReceta` ahora devuelve un Boolean
    suspend fun addReceta(params: RecetaRequest): Boolean {
        val gson = Gson()
        // Crear la entidad LocalReceta, inicialmente como pendiente de sync
        var recetaEntity = LocalReceta(
            idRemoto = null, // Inicialmente null, se actualizará si se sube a la API
            nombre = params.nombre,
            descripcion = params.descripcion,
            tiempo = params.tiempo,
            porciones = params.porciones,
            dificultad = params.dificultad,
            imagen = params.imagen,
            imagenes = gson.toJson(params.imagenes),
            username = params.creadorEmail,
            categoria = params.categoriaId,
            pasosJson = gson.toJson(params.pasos),
            ingredientesJson = gson.toJson(params.ingredientes),
            pendienteDeSync = true // Siempre se marca como pendiente al inicio, se cambia si se sube
        )

        if (isInternetAvailable(context)) {
            try {
                val response = apiService.addReceta(params) // Asumo que apiService.addReceta devuelve Response<Unit> o algo similar
                if (response.isSuccessful) {
                    Log.i("RecetaRepository", "✅ Receta enviada correctamente al servidor")
                    // Si la receta se sube correctamente, obtén el ID remoto si la API lo devuelve
                    // Y GUÁRDALA LOCALMENTE con el idRemoto y pendienteDeSync = false
                    val remoteId = /* Obtén el ID remoto de la respuesta de la API si está disponible */ null
                    recetaEntity = recetaEntity.copy(idRemoto = remoteId, pendienteDeSync = false)
                    recetaDao.insertarReceta(recetaEntity)
                    Log.i("RecetaRepository", "✅ Receta guardada localmente tras éxito en servidor. ID Remoto: $remoteId")
                    return true // Éxito: se subió y se guardó localmente
                } else {
                    // Fallo de respuesta del servidor (ej. 4xx, 5xx)
                    val errorBody = response.errorBody()?.string()
                    Log.e("RecetaRepository", "⚠️ Falló la respuesta del servidor (${response.code()}): ${response.message()}. Error body: $errorBody. Receta guardada localmente como pendiente.")
                    recetaDao.insertarReceta(recetaEntity) // Guardar localmente como pendiente
                    return false // Fallo
                }
            } catch (e: Exception) {
                // Excepción (ej. red, timeout, JSON malformado)
                Log.e("RecetaRepository", "❌ Excepción al enviar receta al servidor: ${e.message}. Receta guardada localmente como pendiente.", e)
                recetaDao.insertarReceta(recetaEntity) // Guardar localmente como pendiente
                return false // Fallo
            }
        } else {
            // No hay internet
            Log.i("RecetaRepository", "📴 Sin conexión, receta guardada localmente como pendiente.")
            recetaDao.insertarReceta(recetaEntity) // Guardar localmente como pendiente
            return false // Fallo (no se pudo subir, solo se guardó localmente)
        }
    }

    // `addRecetaLocal` ahora devuelve un Boolean
    suspend fun addRecetaLocal(params: RecetaRequest): Boolean {
        val gson = Gson()
        val recetaEntity = LocalReceta(
            idRemoto = null,
            nombre = params.nombre,
            descripcion = params.descripcion,
            tiempo = params.tiempo,
            porciones = params.porciones,
            dificultad = params.dificultad,
            imagen = params.imagen, // Aquí 'imagen' puede ser la URI local
            imagenes = gson.toJson(params.imagenes), // Aquí 'imagenes' puede contener URIs locales
            username = params.creadorEmail,
            categoria = params.categoriaId,
            pasosJson = gson.toJson(params.pasos),
            ingredientesJson = gson.toJson(params.ingredientes),
            pendienteDeSync = true // Siempre es true si solo se guarda localmente
        )

        return try {
            recetaDao.insertarReceta(recetaEntity)
            Log.d("RecetaRepository", "📴 Receta guardada localmente exitosamente.")
            Log.d("RecetaRepository", """
            🔒 Datos guardados LOCALMENTE:
            - Nombre: ${recetaEntity.nombre}
            - Descripción: ${recetaEntity.descripcion}
            - Tiempo: ${recetaEntity.tiempo}
            - Porciones: ${recetaEntity.porciones}
            - Dificultad: ${recetaEntity.dificultad}
            - Imagen principal (ruta o URL): ${recetaEntity.imagen}
            - Imagenes: ${recetaEntity.imagenes}
            - Creador: ${recetaEntity.username}
            - Categoría: ${recetaEntity.categoria}
            - Pasos (JSON): ${recetaEntity.pasosJson}
            - Ingredientes (JSON): ${recetaEntity.ingredientesJson}
            - Pendiente de sincronizar: ${recetaEntity.pendienteDeSync}
        """.trimIndent())
            true // Éxito al guardar localmente
        } catch (e: Exception) {
            Log.e("RecetaRepository", "❌ Excepción al guardar receta LOCALMENTE: ${e.message}", e)
            false // Fallo al guardar localmente
        }
    }


    suspend fun subirImagen(context: Context, uri: Uri): String? {
        return try {
            val file = FileUtil.from(context, uri)

            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

            val response = apiService.uploadRecipeImage(body) // Cambiar a uploadRecipeImage
            if (response.isSuccessful) {
                Log.d("RecetaRepository", "Imagen subida exitosamente. URL: ${response.body()?.imageUrl}")
                response.body()?.imageUrl // Acceder a imageUrl del objeto de respuesta
            } else {
                Log.e("RecetaRepository", "Error al subir imagen: ${response.code()} - ${response.message()}")
                null
            }
        } catch (e: Exception) {
            Log.e("RecetaRepository", "Excepción al subir imagen: ${e.message}", e)
            null
        }
    }

    object FileUtil {
        fun from(context: Context, uri: Uri): File {
            val contentResolver = context.contentResolver
            val fileName = getFileName(contentResolver, uri) ?: "temp_image.jpg" // Obtener nombre de archivo o usar uno por defecto
            val file = File(context.cacheDir, fileName)

            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            return file
        }

        private fun getFileName(contentResolver: android.content.ContentResolver, uri: Uri): String? {
            var result: String? = null
            if (uri.scheme == "content") {
                val cursor = contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val displayNameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (displayNameIndex != -1) {
                            result = it.getString(displayNameIndex)
                        }
                    }
                }
            }
            if (result == null) {
                result = uri.path
                val cut = result?.lastIndexOf('/')
                if (cut != -1) {
                    result = result?.substring(cut!! + 1)
                }
            }
            return result
        }
    }

    suspend fun deleteReceta(id: String){
        apiService.deleteReceta(id)
    }

    // Función para verificar la conexión a Internet
    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    suspend fun sincronizarPendientes() {
        if (!isInternetAvailable(context) ) return

        val pendientes = recetaDao.obtenerPendientes()
        for (receta in pendientes) {
            try {
                val gson = Gson()

                val pasosOriginales = gson.fromJson<List<PasoRecetaRequest>>(
                    receta.pasosJson,
                    object : TypeToken<List<PasoRecetaRequest>>() {}.type
                )

                val ingredientes = gson.fromJson<List<Ingrediente>>(
                    receta.ingredientesJson,
                    object : TypeToken<List<Ingrediente>>() {}.type
                )

                // Subir imagen principal si es ruta local
                val imagenPrincipalUrl = if (receta.imagen?.startsWith("/") == true || receta.imagen?.startsWith("file://") == true) {
                    subirImagen(context, Uri.fromFile(File(receta.imagen))) ?: ""
                } else {
                    receta.imagen ?: ""
                }

                // Subir imagenes principales
                val imagenesPrincipalesLocales: List<String> = try {
                    gson.fromJson<List<String>>(receta.imagenes ?: "[]", object : TypeToken<List<String>>() {}.type)
                } catch (e: Exception) {
                    emptyList()
                }

                val imagenesPrincipalesUrls = imagenesPrincipalesLocales.mapNotNull { imagenPathRaw ->
                    val imagenPath = imagenPathRaw as? String ?: return@mapNotNull null

                    if ((imagenPath.startsWith("/") || imagenPath.startsWith("file://")) && File(imagenPath).exists()) {
                        subirImagen(context, Uri.fromFile(File(imagenPath))) ?: imagenPath
                    } else {
                        imagenPath
                    }
                }


                // Subir imágenes de pasos si son locales
                val pasosActualizados = pasosOriginales.map { paso ->
                    val nuevaArchivoFoto = if (paso.archivoFoto?.startsWith("/") == true || paso.archivoFoto?.startsWith("file://") == true) {
                        subirImagen(context, Uri.fromFile(File(paso.archivoFoto))) ?: paso.archivoFoto
                    } else {
                        paso.archivoFoto
                    }

                    val nuevasImagenes = paso.imagenesPasos?.mapNotNull { imagenPathRaw ->
                        val imagenPath = imagenPathRaw as? String ?: return@mapNotNull null

                        if (File(imagenPath).exists() && imagenPath.startsWith("/") || imagenPath.startsWith("file://")) {
                            subirImagen(context, Uri.fromFile(File(imagenPath))) ?: imagenPath
                        } else {
                            imagenPath
                        }
                    }

                    PasoRecetaRequest(
                        numeroDePaso = paso.numeroDePaso,
                        contenido = paso.contenido,
                        archivoFoto = nuevaArchivoFoto,
                        imagenesPasos = nuevasImagenes
                    )
                }

                val request = RecetaRequest(
                    nombre = receta.nombre,
                    descripcion = receta.descripcion,
                    tiempo = receta.tiempo ?: "",
                    porciones = receta.porciones,
                    dificultad = receta.dificultad,
                    categoriaId = receta.categoria,
                    imagen = imagenPrincipalUrl,
                    imagenes = imagenesPrincipalesUrls, // si vas a permitir múltiples imágenes principales, aquí también podés subirlas
                    creadorEmail = receta.username,
                    ingredientes = ingredientes,
                    pasos = pasosActualizados,
                    estado = "ACTIVA"
                )

                val response = apiService.addReceta(request)
                if (response.isSuccessful) {
                    recetaDao.marcarComoSincronizada(receta.localId)
                    Log.d("Sync", "✅ Receta sincronizada correctamente: ${receta.nombre}")
                } else {
                    Log.e("Sync", "❌ Error al sincronizar receta: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("Sync", "⚠️ Error al procesar receta pendiente: ${e.message}", e)
                // Sigue pendiente
            }
        }
    }

    suspend fun editarReceta(params: RecetaRequest, id: String): Boolean {
        return try {
            val gson = Gson()
            val recetaEntity = LocalReceta(
                idRemoto = null,
                nombre = params.nombre,
                descripcion = params.descripcion,
                tiempo = params.tiempo,
                porciones = params.porciones,
                dificultad = params.dificultad,
                imagen = params.imagen,
                imagenes = gson.toJson(params.imagenes),
                username = params.creadorEmail,
                categoria = params.categoriaId,
                pasosJson = gson.toJson(params.pasos),
                ingredientesJson = gson.toJson(params.ingredientes),
                pendienteDeSync = true
            )
            val response =apiService.editarReceta(params,id)
            if (response.isSuccessful) {
                Log.i("AddReceta", "✅ Receta enviada correctamente al servidor")
                true // Éxito
            } else {
                recetaDao.insertarReceta(recetaEntity)
                Log.i("AddReceta", "⚠️ Falló la respuesta del servidor")
                false // Fallo por respuesta de error de la API
            }
        } catch (e: Exception) {
            Log.e("RecetaRepository", "Excepción de red/parseo al editar receta ID $id: ${e.message}", e)
            false // Fallo por excepción
        }
    }

    suspend fun editarEstadoComentario(estadoParams: EstadoRequest,id: Int): Boolean{
        return try {
            // Asumo que apiService.cambiarEstadoComentario devuelve Response<Unit> o Response<SomeSuccessDto>
            val response = apiService.cambiarEstadoComentario(estadoParams, id.toString())

            if (response.isSuccessful) {
                // La API respondió con un código de éxito (ej. 200, 204)
                Log.d("RecetaRepository", "✅ Estado de la receta ID $id cambiado exitosamente a ${estadoParams.estado}")
                true // Éxito
            } else {
                // La API respondió con un código de error (ej. 4xx, 5xx)
                val errorBody = response.errorBody()?.string()
                Log.e("RecetaRepository", "⚠️ Fallo al cambiar estado de receta ID $id: ${response.code()} - ${response.message()}. Cuerpo de error: $errorBody")
                false // Fallo por respuesta de error de la API
            }
        } catch (e: Exception) {
            // Ocurrió una excepción (ej. error de red, timeout, JSON malformado)
            Log.e("RecetaRepository", "❌ Excepción al cambiar estado de receta ID $id: ${e.message}", e)
            false // Fallo por excepción
        }
    }

    suspend fun editarEstadoReceta(estadoParams: EstadoRequest, id: Int): Boolean {
        return try {
            // Asumo que apiService.cambiarEstadoComentario devuelve Response<Unit> o Response<SomeSuccessDto>
            val response = apiService.cambiarEstadoReceta(estadoParams, id.toString())

            if (response.isSuccessful) {
                // La API respondió con un código de éxito (ej. 200, 204)
                Log.d("RecetaRepository", "✅ Estado de la receta ID $id cambiado exitosamente a ${estadoParams.estado}")
                true // Éxito
            } else {
                // La API respondió con un código de error (ej. 4xx, 5xx)
                val errorBody = response.errorBody()?.string()
                Log.e("RecetaRepository", "⚠️ Fallo al cambiar estado de receta ID $id: ${response.code()} - ${response.message()}. Cuerpo de error: $errorBody")
                false // Fallo por respuesta de error de la API
            }
        } catch (e: Exception) {
            // Ocurrió una excepción (ej. error de red, timeout, JSON malformado)
            Log.e("RecetaRepository", "❌ Excepción al cambiar estado de receta ID $id: ${e.message}", e)
            false // Fallo por excepción
        }
    }

    suspend fun getComentariosPendientes(): List<Comentario> ?{
        val response = apiService.getComentariosPendientes()
        return if (response.isSuccessful && response.body() != null) {
            Log.d("RecetasRepository", "Comentario recibida correctamente")
            //Log.d("RecetaRepositoryComentario", response.body().toString())
            return response.body()!!.map { it.toComentario() }
        } else {
            Log.e("RecetasRepository", "Error al obtener Comentario: ${response.code()} - ${response.message()}")
            null
        }
    }

    suspend fun getRecetasPendientes(): List<Receta> ?{
        val response = apiService.getRecetasPendientes()
        return if (response.isSuccessful && response.body() != null) {
            Log.d("RecetasRepository", "Receta recibida correctamente")
            return response.body()!!.map { it.toReceta() }

        } else {
            Log.e("RecetasRepository", "Error al obtener Receta: ${response.code()} - ${response.message()}")
            null
        }
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

    // Nuevo método para guardar la receta ajustada localmente
    suspend fun guardarRecetaAjustadaLocal(recetaAjustada: Receta): Boolean {
        return try {
            val currentLocalCount = recetaDao.contarRecetasLocales()

            if (currentLocalCount >= MAX_LOCAL_ADJUSTED_RECIPES) {
                Log.w("RecetaRepository", "Límite de recetas locales alcanzado ($MAX_LOCAL_ADJUSTED_RECIPES). No se puede guardar.")
                // Opcional: Eliminar la receta más antigua si quieres un sistema FIFO (First-In, First-Out)
                // val oldestRecipe = recetaDao.obtenerRecetaMasAntigua() // Necesitas implementar este método en DAO
                // oldestRecipe?.let { recetaDao.eliminarReceta(it) }
                // Log.i("RecetaRepository", "Receta más antigua eliminada para hacer espacio.")
                // recetaDao.insertarReceta(recetaEntity) // Luego intenta insertar la nueva
                return false // Indica que no se pudo guardar por el límite
            }

            val gson = Gson()
            // Mapea tu Receta de dominio a tu LocalReceta de Room
            val localReceta = LocalReceta(
                idRemoto = recetaAjustada.id, // Guarda el ID remoto si existe (para referencia)
                nombre = recetaAjustada.nombre,
                descripcion = recetaAjustada.descripcion,
                tiempo = recetaAjustada.tiempo,
                porciones = recetaAjustada.porciones.toString(), // Convertir Int a String
                dificultad = recetaAjustada.dificultad.label, // Convertir Enum a String
                imagen = recetaAjustada.imagen,
                imagenes = gson.toJson(recetaAjustada.imagenes?.map { it.url }), // Serializar List<MediaItem> a JSON String de URLs
                username = recetaAjustada.username,
                categoria = recetaAjustada.categoria,
                pasosJson = gson.toJson(recetaAjustada.pasos), // Serializar List<PasoReceta>
                ingredientesJson = gson.toJson(recetaAjustada.ingredientes), // Serializar List<Ingrediente>
                pendienteDeSync = false // Estas recetas ajustadas NO están pendientes de sincronizar con la API remota
                // Si quieres un campo 'esRecetaAjustada: Boolean' en LocalReceta, aquí iría a 'true'
            )

            recetaDao.insertarReceta(localReceta)
            Log.d("RecetaRepository", "✅ Receta ajustada '${localReceta.nombre}' guardada localmente. Total: ${currentLocalCount + 1}")
            return true
        } catch (e: Exception) {
            Log.e("RecetaRepository", "❌ Error al guardar receta ajustada localmente: ${e.message}", e)
            return false
        }
    }

    // Método para obtener solo las recetas ajustadas/guardadas localmente
    // Esto es vital si quieres una sección "Mis Recetas Guardadas" offline
    suspend fun obtenerRecetasAjustadasLocales(): List<Receta> {
        return try {
            val localRecetas = recetaDao.obtenerTodas().filter { !it.pendienteDeSync } // Filtra las que no están pendientes de sync
            // Si solo guardas las ajustadas con pendienteDeSync=false, esto funciona.
            // Si usas un campo 'esRecetaAjustada', tendrías que filtrarlas por ese campo.

            val gson = Gson()
            localRecetas.map { local ->
                // Deserializar JSON strings de vuelta a objetos
                val ingredientes = gson.fromJson(local.ingredientesJson, object : TypeToken<List<Ingrediente>>() {}.type) as List<Ingrediente>
                val pasos = gson.fromJson(local.pasosJson, object : TypeToken<List<PasoReceta>>() {}.type) as List<PasoReceta>
                val imagenesUrls = gson.fromJson(local.imagenes, object : TypeToken<List<String>>() {}.type) as List<String>
                val mediaItems = imagenesUrls.map { MediaItem(url = it, type = MediaType.IMAGE) } // Convertir URLs a MediaItem

                Receta(
                    id = local.idRemoto ?: 0, // Si no tiene ID remoto, usa 0 (puede ser el de la API si se guardó una API-Receta)
                    localId = local.localId,// Si no tiene ID remoto, usa 0 o maneja null
                    nombre = local.nombre,
                    descripcion = local.descripcion,
                    tiempo = local.tiempo,
                    porciones = local.porciones.toInt(),
                    dificultad = Dificultad.valueOf(local.dificultad.uppercase()),
                    imagen = local.imagen,
                    estado = RecetaStatus.APROBADA, // O el estado que desees para las recetas guardadas
                    username = local.username,
                    categoria = local.categoria,
                    reviewCount = 0, // Por defecto 0, ya que no son de la API
                    averageRating = 0f, // Por defecto 0f
                    pasos = pasos,
                    ingredientes = ingredientes,
                    comentarios = emptyList(), // No hay comentarios para estas recetas guardadas
                    imagenes = mediaItems
                )
            }
        } catch (e: Exception) {
            Log.e("RecetaRepository", "❌ Error al obtener recetas ajustadas localmente: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun eliminarRecetaLocal(receta: Receta): Boolean {
        return try {
            // Necesitas el localId para eliminar de Room.
            // Si tu Receta de dominio no tiene localId, necesitarás buscar la LocalReceta
            // por un campo único como 'nombre' y 'username'.
            // La forma más robusta es que tu Receta de dominio también guarde el localId si la obtuviste de Room.
            // Por simplicidad, asumamos que puedes buscar la LocalReceta por nombre y username para eliminar.

            // 1. Busca la LocalReceta correspondiente
            val localRecetaToDelete = recetaDao.obtenerTodas()
                .firstOrNull { it.nombre == receta.nombre && it.username == receta.username }

            localRecetaToDelete?.let {
                recetaDao.eliminarReceta(it)
                Log.d("RecetaRepository", "✅ Receta local '${receta.nombre}' eliminada exitosamente.")
                true
            } ?: run {
                Log.e("RecetaRepository", "❌ Receta local '${receta.nombre}' no encontrada para eliminar.")
                false
            }
        } catch (e: Exception) {
            Log.e("RecetaRepository", "❌ Error al eliminar receta local '${receta.nombre}': ${e.message}", e)
            false
        }
    }

    suspend fun obtenerRecetaLocalPorLocalId(localId: Int): Receta? {
        return try {
            val localReceta = recetaDao.obtenerPorLocalId(localId)
            if (localReceta != null) {
                Log.d("RecetaRepository", "DAO encontró LocalReceta con ID $localId: ${localReceta.nombre}")
                // Mapear de LocalReceta a Receta de dominio
                val gson = Gson()
                val ingredientes = gson.fromJson<List<Ingrediente>>(localReceta.ingredientesJson, object : TypeToken<List<Ingrediente>>() {}.type)
                val pasos = gson.fromJson<List<PasoReceta>>(localReceta.pasosJson, object : TypeToken<List<PasoReceta>>() {}.type)
                val imagenesUrls = gson.fromJson<List<String>>(localReceta.imagenes, object : TypeToken<List<String>>() {}.type)
                val mediaItems = imagenesUrls.map { MediaItem(url = it, type = MediaType.IMAGE) }

                val mappedReceta = Receta(
                    id = localReceta.idRemoto ?: 0,
                    localId = localReceta.localId, // Asegúrate de que este campo está en tu Receta de dominio
                    nombre = localReceta.nombre,
                    descripcion = localReceta.descripcion,
                    tiempo = localReceta.tiempo,
                    porciones = localReceta.porciones.toInt(),
                    dificultad = Dificultad.valueOf(localReceta.dificultad.uppercase()),
                    imagen = localReceta.imagen,
                    estado = RecetaStatus.APROBADA, // O el estado que decidas
                    username = localReceta.username,
                    categoria = localReceta.categoria,
                    reviewCount = 0,
                    averageRating = 0f,
                    pasos = pasos,
                    ingredientes = ingredientes,
                    comentarios = emptyList(),
                    imagenes = mediaItems
                )
                Log.d("RecetaRepository", "LocalReceta ID $localId mapeada a Receta: ${mappedReceta.nombre}")
                mappedReceta
            } else {
                Log.w("RecetaRepository", "DAO no encontró LocalReceta con ID: $localId")
                null
            }
        } catch (e: Exception) {
            Log.e("RecetaRepository", "❌ Error al mapear LocalReceta a Receta para ID $localId: ${e.message}", e)
            null
        }
    }
}





