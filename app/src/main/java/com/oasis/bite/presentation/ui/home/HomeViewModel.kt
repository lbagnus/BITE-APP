package com.oasis.bite.presentation.ui.home

import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oasis.bite.data.model.EstadoRequest
import com.oasis.bite.data.model.FavParams
import com.oasis.bite.data.model.FavRequest
import com.oasis.bite.data.model.RecetaSearchParams
import com.oasis.bite.data.repository.RecetaRepository
import com.oasis.bite.domain.models.Category
import com.oasis.bite.domain.models.Comentario
import com.oasis.bite.domain.models.Receta
import com.oasis.bite.localdata.database.dao.RecetaDao
import kotlinx.coroutines.launch

class HomeViewModel(private val repositoryReceta: RecetaRepository) : ViewModel() {

    val recetasLiveData = MutableLiveData<List<Receta>?>()
    val categoryLiveData = MutableLiveData<List<Category>?>()

    private val _pendingComments = MutableLiveData<List<Comentario>?>()
    val pendingComments: LiveData<List<Comentario>> get() = _pendingComments as LiveData<List<Comentario>>

    private val _pendingRecetas = MutableLiveData<List<Receta>?>()
    val pendingReceta: LiveData<List<Receta>> get() = _pendingRecetas as LiveData<List<Receta>>

    private val _commentOperationStatus = MutableLiveData<Boolean>()
    val commentOperationStatus: LiveData<Boolean> get() = _commentOperationStatus

    private val _recetaOperationStatus = MutableLiveData<Boolean>()
    val recetaOperationStatus: LiveData<Boolean> get() = _recetaOperationStatus

    val misRecetasLiveData = MutableLiveData<List<Receta>?>()

    private val _favoritoLiveData = MutableLiveData<List<Receta>>()
    val favoritoLiveData: LiveData<List<Receta>> get() = _favoritoLiveData as LiveData<List<Receta>>

    // LiveData para feedback al usuario si la operación de favoritos falla (opcional, pero recomendado)
    private val _favoritoOperationStatus = MutableLiveData<Boolean>()
    val favoritoOperationStatus: LiveData<Boolean> get() = _favoritoOperationStatus
    private val _favoritoOperationMessage = MutableLiveData<String>()
    val favoritoOperationMessage: LiveData<String> get() = _favoritoOperationMessage


    fun cargarRecetas() {
        viewModelScope.launch {
            val recetas = repositoryReceta.getRecetasHome() // desde tu API
            recetasLiveData.postValue(recetas)
        }
    }

    fun cargarRecetasPorCategoria(categoria: String) {
        viewModelScope.launch {
            val params = RecetaSearchParams(
                type = categoria, // 👈 acá pasás la categoría
                limit = 20,
                offset = 0
            )

            val recetas = repositoryReceta.getRecetasSearch(params)
            recetasLiveData.postValue(recetas ?: emptyList())
        }
    }
    fun cargarRecetasUsuario(email: String) {
        viewModelScope.launch {
            val recetas = repositoryReceta.getRecetasUsuario(email)
            misRecetasLiveData.postValue(recetas ?: emptyList())
        }
    }

    fun getRecetasFavoritos(email: String, onResult: (List<Receta>) -> Unit) {
        viewModelScope.launch {
            try {
                val favoritos = repositoryReceta.getRecetasFavoritos(email)
                _favoritoLiveData.postValue(favoritos ?: emptyList())
                onResult(favoritos ?: emptyList()) // Este callback es para HomeFragment y otros
                Log.d("HomeViewModel", "Favoritos cargados: ${favoritos?.size ?: 0}")
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error al cargar recetas favoritas: ${e.message}", e)
                _favoritoLiveData.postValue(emptyList()) // En caso de error, una lista vacía
                onResult(emptyList()) // Notifica el error a través del callback también
                _favoritoOperationStatus.postValue(false)
                _favoritoOperationMessage.postValue("Error al cargar tus favoritos.")
            }
        }
    }

    fun eliminarRecetaFavorito(email: String, recetaId: Int) {
        viewModelScope.launch {
            try {
                val params = FavParams(email = email, recetaId = recetaId)
                // Asumo que repositoryReceta.deleteFavorito devuelve un Boolean o Response<Unit>
                val success = repositoryReceta.deleteFavorito(params) // Llamas al repositorio

                if (success) {
                    Log.d("HomeViewModel", "Receta ID $recetaId eliminada de favoritos exitosamente.")
                    _favoritoOperationStatus.postValue(true)
                    _favoritoOperationMessage.postValue("Receta eliminada de favoritos.")
                    // ¡CLAVE!: Recargar la lista de favoritos después de la eliminación exitosa
                    getRecetasFavoritos(email) { /* no-op, el LiveData ya se actualiza */ }
                } else {
                    Log.e("HomeViewModel", "Fallo al eliminar receta ID $recetaId de favoritos. Repositorio indicó fallo.")
                    _favoritoOperationStatus.postValue(false)
                    _favoritoOperationMessage.postValue("No se pudo eliminar de favoritos. Inténtalo de nuevo.")
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Excepción al eliminar receta ID $recetaId de favoritos: ${e.message}", e)
                _favoritoOperationStatus.postValue(false)
                _favoritoOperationMessage.postValue("Error de conexión al eliminar de favoritos.")
            }
        }
    }

    fun agregarRecetaFavorito(email: String, recetaId: Int) {
        viewModelScope.launch {
            try {
                val params = FavParams(email = email, recetaId = recetaId)
                // Asumo que repositoryReceta.addFavorito devuelve un Boolean o Response<Unit>
                val success = repositoryReceta.addFavorito(params) // Llamas al repositorio

                if (success) {
                    Log.d("HomeViewModel", "Receta ID $recetaId agregada a favoritos exitosamente.")
                    _favoritoOperationStatus.postValue(true)
                    _favoritoOperationMessage.postValue("Receta agregada a favoritos.")
                    // ¡CLAVE!: Recargar la lista de favoritos después de la adición exitosa
                    getRecetasFavoritos(email) { /* no-op, el LiveData ya se actualiza */ }
                } else {
                    Log.e("HomeViewModel", "Fallo al agregar receta ID $recetaId a favoritos. Repositorio indicó fallo.")
                    _favoritoOperationStatus.postValue(false)
                    _favoritoOperationMessage.postValue("No se pudo agregar a favoritos. Inténtalo de nuevo.")
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Excepción al agregar receta ID $recetaId a favoritos: ${e.message}", e)
                _favoritoOperationStatus.postValue(false)
                _favoritoOperationMessage.postValue("Error de conexión al agregar a favoritos.")
            }
        }
    }

    fun syncRecetasLocales() {
        viewModelScope.launch {
            repositoryReceta.sincronizarPendientes()
        }
    }

    fun cargarRecetasSearch(termino: String) {
        viewModelScope.launch {
            val recetas = repositoryReceta.getBySearch(termino)
            recetasLiveData.postValue(recetas)
        }
    }

    // --------- Método nuevo para filtrar recetas ---------
    fun cargarRecetasConFiltro(
        incluye: List<String>,
        excluye: List<String>,
        direction: String,
        username: String? = null
    ) {
        viewModelScope.launch {
            val params = RecetaSearchParams(
                includeIngredients = incluye,
                excludeIngredients = excluye,
                orderBy = "newest",
                direction = direction,
                userName = username,
                limit = 20,
                offset = 0
            )
            Log.d("FiltroAPI", "Llamando API con filtros: $params")
            val recetas = repositoryReceta.getRecetasSearch(params)
            Log.d("FiltroAPI", "Recetas filtradas recibidas: ${recetas?.size}")
            recetasLiveData.postValue(recetas ?: emptyList())
        }
    }
    // ----------------------------------------------------


    fun editarEstadoComentario(id:Int, estado: String){
        viewModelScope.launch{
            var estadoParams = EstadoRequest(estado = estado)
            try {
                val success = repositoryReceta.editarEstadoComentario(estadoParams,id)
                if (success) {
                    Log.d("HomeViewModel", "Comentario ID $id rechazado exitosamente.")
                    _commentOperationStatus.postValue(true)
                    loadPendingComments()
                } else {
                    Log.e("HomeViewModel", "Fallo al rechazar comentario ID $id.")
                    _commentOperationStatus.postValue(false)
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Excepción al rechazar comentario ID $id: ${e.message}", e)
                _commentOperationStatus.postValue(false)
            }

        }}

    fun editarEstadoReceta(id:Int, estado: String){
        viewModelScope.launch{
            var estadoParams = EstadoRequest(estado = estado)
                try {
                    val success = repositoryReceta.editarEstadoReceta(estadoParams,id)
                    if (success) {
                        Log.d("HomeViewModel", "receta ID $id rechazado exitosamente.")
                        _recetaOperationStatus.postValue(true)
                        loadPendingComments() //
                    } else {
                        Log.e("HomeViewModel", "Fallo al rechazar receta ID $id.")
                        _recetaOperationStatus.postValue(false)
                    }
                } catch (e: Exception) {
                    Log.e("HomeViewModel", "Excepción al rechazar receta ID $id: ${e.message}", e)
                    _recetaOperationStatus.postValue(false)
                }

        }
    }

    fun loadPendingComments() {
        viewModelScope.launch {
            try {
                val pendingCommentsList = repositoryReceta.getComentariosPendientes()
                _pendingComments.postValue(pendingCommentsList)
                Log.d("HomeViewModel", "Comentarios pendientes cargados: ${pendingCommentsList?.size ?: 0}")
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error al cargar comentarios pendientes: ${e.message}", e)
                _pendingComments.postValue(emptyList())
            }
        }
    }

    fun loadPendingRecetas() {
        viewModelScope.launch {
            try {
                val pendingRecetasList = repositoryReceta.getRecetasPendientes()
                _pendingRecetas.postValue(pendingRecetasList)
                Log.d("HomeViewModel", "Recetas pendientes cargados: ${pendingRecetasList?.size ?: 0}")
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error al cargar Recetas pendientes: ${e.message}", e)
                _pendingRecetas.postValue(emptyList())
            }
        }
    }
    fun eliminarReceta(id: String){
        viewModelScope.launch {
            repositoryReceta.deleteReceta(id = id)
        }
    }
}
