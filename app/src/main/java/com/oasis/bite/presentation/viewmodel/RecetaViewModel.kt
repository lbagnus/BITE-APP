package com.oasis.bite.presentation.viewmodel

import android.R
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oasis.bite.data.RetrofitInstance
import com.oasis.bite.data.model.CommentRequest
import com.oasis.bite.data.model.FavParams
import com.oasis.bite.data.model.PasoRecetaRequest
import com.oasis.bite.data.model.RecetaRequest
import com.oasis.bite.data.model.RecetaSearchParams
import com.oasis.bite.data.repository.RecetaRepository
import com.oasis.bite.data.repository.UserRepository
import com.oasis.bite.domain.models.Receta
import com.oasis.bite.domain.models.Ingrediente
import com.oasis.bite.domain.models.MediaItem
import com.oasis.bite.domain.models.PasoReceta
import com.oasis.bite.domain.models.RecetaStatus
import kotlinx.coroutines.launch

class RecetaViewModel(private val repository: RecetaRepository) : ViewModel() {


    private val _receta = MutableLiveData<Receta?>()
    val receta: MutableLiveData<Receta?> get() = _receta

    private val _ingredientes = MutableLiveData<List<Ingrediente>>()
    val ingredientes: LiveData<List<Ingrediente>> get() = _ingredientes

    private val _pasos = MutableLiveData<List<PasoReceta>>()
    val pasos: LiveData<List<PasoReceta>> get() = _pasos
    val favoritoLiveData = MutableLiveData<List<Receta>?>()
    private val _recetaOperationStatus = MutableLiveData<Boolean>()
    val recetaOperationStatus: LiveData<Boolean> get() = _recetaOperationStatus // True para éxito, False para fallo


    fun cargarReceta(id: String) {
        viewModelScope.launch {
            try {
                val recetaCargada = repository.getRecetaPorId(id)
                _receta.value = recetaCargada
                _ingredientes.value = recetaCargada?.ingredientes ?: emptyList()
                _pasos.value = recetaCargada?.pasos ?: emptyList()
            } catch (e: Exception) {
                Log.e("RecetaViewModel", "Error al cargar receta", e)
            }
        }
    }

    fun getRecetasFavoritos(email: String, onResult: (List<Receta>) -> Unit) {
        viewModelScope.launch {
            val favoritos = repository.getRecetasFavoritos(email)
            favoritoLiveData.postValue(favoritos ?:emptyList())
        }
    }

    fun eliminarRecetaFavorito(email: String, receta: Int){
        viewModelScope.launch{
            var params = FavParams(email = email, recetaId =receta)
            repository.deleteFavorito(params)}
    }

    fun agregarRecetaFavorito(email: String, receta: Int){
        viewModelScope.launch{
            var params = FavParams(email = email, recetaId =receta)
            repository.addFavorito(params)}
    }

    fun agregarComentario(email: String, titulo: String, reseña: String, valoracion: Int, receta: Int): Int{
        var response = 0
        viewModelScope.launch{
            var params = CommentRequest(usuarioEmail = email, recetaId =receta, titulo = titulo, reseña = reseña, valoracion = valoracion)
                response =  repository.addComentario(params)}
        return response
    }

    fun verificarSiRecetaExiste(
        titulo: String,
        usuario: String,
        callback: (Int) -> Unit
    ) {
        viewModelScope.launch {
            val params = RecetaSearchParams(
                userName = usuario,
                limit = 20,
                offset = 0
            )
            val recetas = repository.getRecetasSearch(params)
            Log.d("verificarReceta", titulo)
            val recetaExistente = recetas?.find {
                it.nombre.equals(titulo, ignoreCase = true)
            }
            Log.d("verificarReceta", recetaExistente?.id.toString())
            callback(recetaExistente?.id ?: 0)
        }
    }


    fun agregarReceta(
        nombre: String,
        descripcion: String,
        tiempo: String,
        porciones: String,
        dificultad: String,
        imagen: String, // Puede ser String? para URLs
        imagenes: List<String>, // Lista de URLs
        creadorEmail: String,
        ingredientes: List<Ingrediente>,
        pasos: List<PasoRecetaRequest>,
        categoria: String
    ) {
        viewModelScope.launch {
            try {
                val params = RecetaRequest(
                    nombre = nombre,
                    descripcion = descripcion,
                    tiempo = tiempo,
                    porciones = porciones,
                    dificultad = dificultad,
                    imagen = imagen,
                    imagenes = imagenes,
                    creadorEmail = creadorEmail,
                    ingredientes = ingredientes,
                    pasos = pasos,
                    estado = "Aprobada",
                    categoriaId = categoria // Asumo que este es el nombre del campo en tu RecetaRequest
                )

                // Llama al método del repositorio que interactúa con la API remota
                val success = repository.addReceta(params) // Asume que addReceta en el repo devuelve un Boolean

                if (success) {
                    Log.d("RecetaViewModel", "Receta '${nombre}' creada exitosamente en la API.")
                    _recetaOperationStatus.postValue(true) // Notifica éxito a la UI
                } else {
                    Log.e("RecetaViewModel", "Fallo al crear la receta '${nombre}' en la API. El repositorio indicó fallo.")
                    _recetaOperationStatus.postValue(false) // Notifica fallo a la UI
                }
            } catch (e: Exception) {
                // Captura cualquier excepción que ocurra durante la llamada a la API (ej. red, JSON)
                Log.e("RecetaViewModel", "Excepción al crear receta '${nombre}' en la API: ${e.message}", e)
                _recetaOperationStatus.postValue(false) // Notifica fallo a la UI
            }
        }
    }

    fun agregarRecetaOffline(
        nombre: String,
        descripcion: String,
        tiempo: String,
        porciones: String,
        dificultad: String,
        imagen: String, // Aquí imagen podría ser la URI local
        imagenes: List<String>, // Aquí las imágenes podrían ser URIs locales
        creadorEmail: String,
        ingredientes: List<Ingrediente>,
        pasos: List<PasoRecetaRequest>,
        categoria: String
    ) {
        viewModelScope.launch {
            try {
                val params = RecetaRequest(
                    nombre = nombre,
                    descripcion = descripcion,
                    tiempo = tiempo,
                    porciones = porciones,
                    dificultad = dificultad,
                    imagen = imagen, // Se guarda la URI local
                    imagenes = imagenes, // Se guardan las URIs locales
                    creadorEmail = creadorEmail,
                    ingredientes = ingredientes,
                    pasos = pasos,
                    estado = "Pendiente", // O un estado que indique que es offline y necesita sincronizar
                    categoriaId = categoria
                )

                // Llama al método del repositorio que guarda localmente (Room/otra BD local)
                val success = repository.addRecetaLocal(params) // Asume que addRecetaLocal devuelve un Boolean

                if (success) {
                    Log.d("RecetaViewModel", "Receta '${nombre}' guardada localmente exitosamente.")
                    _recetaOperationStatus.postValue(true) // Notifica éxito a la UI
                    // Opcional: Podrías tener un LiveData diferente para notificar éxito offline
                    // _offlineSaveStatus.postValue(true)
                } else {
                    Log.e("RecetaViewModel", "Fallo al guardar la receta '${nombre}' localmente. El repositorio indicó fallo.")
                    _recetaOperationStatus.postValue(false) // Notifica fallo a la UI
                }
            } catch (e: Exception) {
                // Captura cualquier excepción que ocurra durante el guardado local
                Log.e("RecetaViewModel", "Excepción al guardar receta '${nombre}' localmente: ${e.message}", e)
                _recetaOperationStatus.postValue(false) // Notifica fallo a la UI
            }
        }
    }

    fun subirImagen(context: Context, uri: Uri, callback: (String?) -> Unit) {
        viewModelScope.launch {
            val url = repository.subirImagen(context, uri)
            callback(url)
        }
    }

    fun eliminarReceta(id: String){
        viewModelScope.launch {
            repository.deleteReceta(id = id)
        }
    }

    // En tu RecetaViewModel.kt
    fun editarReceta(
        id: String, // El ID de la receta a editar
        nombre: String,
        descripcion: String,
        tiempo: String,
        porciones: String,
        dificultad: String,
        imagen: String, // La URL de la imagen principal (puede ser nula)
        imagenes: List<String>, // La lista de URLs de imágenes
        creadorEmail: String,
        ingredientes: List<Ingrediente>,
        pasos: List<PasoRecetaRequest>,
        categoria: String
    ) {
        viewModelScope.launch {
            try {
                val params = RecetaRequest(
                    nombre = nombre,
                    descripcion = descripcion,
                    tiempo = tiempo,
                    porciones = porciones,
                    dificultad = dificultad,
                    imagen = imagen,
                    imagenes = imagenes,
                    creadorEmail = creadorEmail,
                    ingredientes = ingredientes,
                    pasos = pasos,
                    estado = "Aprobada",
                    categoriaId = categoria // Asumo que `categoriaId` es el nombre del campo en tu RecetaRequest
                )

                // Llama al método del repositorio para editar la receta
                val success = repository.editarReceta(params, id) // Asumo que editarReceta en el repositorio devuelve un Boolean

                if (success) {
                    // Si la edición fue exitosa
                    Log.d("RecetaViewModel", "Receta ID $id editada exitosamente.")
                    _recetaOperationStatus.postValue(true)
                } else {
                    // Si la edición falló (por ejemplo, el repositorio devolvió 'false')
                    Log.e("RecetaViewModel", "Fallo al editar la receta ID $id. El repositorio indicó fallo.")
                    _recetaOperationStatus.postValue(false)
                }
            } catch (e: Exception) {
                // Si ocurre una excepción (ej. error de red, JSON malformado, etc.)
                Log.e("RecetaViewModel", "Excepción al editar receta ID $id: ${e.message}", e)
                _recetaOperationStatus.postValue(false)
            }
        }
    }
    fun loadWifiPreference(userEmail: String): Boolean {
        var preference = false
        viewModelScope.launch {
            try {
                // Asumo que tu UserRepository tiene un método para obtener la preferencia del switch
                 preference = repository.getWifiPreference(userEmail)

                Log.d("UsersViewModel", "Preferencia WiFi cargada: $preference")
            } catch (e: Exception) {
                Log.e("UsersViewModel", "Error al cargar preferencia WiFi: ${e.message}", e)
                // Podrías poner un valor por defecto o manejar el error en la UI

            }
        }
        return preference
    }


}

