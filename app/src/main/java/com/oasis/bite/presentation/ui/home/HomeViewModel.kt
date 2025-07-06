package com.oasis.bite.presentation.ui.home

import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oasis.bite.data.RetrofitInstance
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

class HomeViewModel( private val repositoryReceta: RecetaRepository) : ViewModel() {
    private val apiService = RetrofitInstance.apiService

    val recetasLiveData = MutableLiveData<List<Receta>?>()

    val categoryLiveData = MutableLiveData<List<Category>?>()

    val favoritoLiveData = MutableLiveData<List<Receta>?>()

    private val _pendingComments = MutableLiveData<List<Comentario>?>()
    val pendingComments: LiveData<List<Comentario>> get() = _pendingComments as LiveData<List<Comentario>>

    private val _pendingRecetas = MutableLiveData<List<Receta>?>()
    val pendingReceta: LiveData<List<Receta>> get() = _pendingRecetas as LiveData<List<Receta>>

    // LiveData para notificar el estado de la operación de comentario (opcional, pero útil)
    private val _commentOperationStatus = MutableLiveData<Boolean>()
    val commentOperationStatus: LiveData<Boolean> get() = _commentOperationStatus

    private val _recetaOperationStatus = MutableLiveData<Boolean>()
    val recetaOperationStatus: LiveData<Boolean> get() = _recetaOperationStatus

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
    fun cargarRecetasUsuario(usuario: String) {
        viewModelScope.launch {
            val params = RecetaSearchParams(
                userName = usuario, // 👈 acá pasás la categoría
                limit = 20,
                offset = 0
            )

            val recetas = repositoryReceta.getRecetasSearch(params)
            recetasLiveData.postValue(recetas ?: emptyList())
        }
    }
    fun cargarRecetasFavoritos(email: String){
        viewModelScope.launch {
            val recetas = repositoryReceta.getRecetasFavoritos(email)
            favoritoLiveData.postValue(recetas ?:emptyList())
        }
    }
    fun getRecetasFavoritos(email: String, onResult: (List<Receta>) -> Unit) {
        viewModelScope.launch {
            val favoritos = repositoryReceta.getRecetasFavoritos(email)
            favoritoLiveData.postValue(favoritos ?:emptyList())
        }
    }

    fun eliminarRecetaFavorito(email: String, receta: Int){
        viewModelScope.launch{
            var params = FavParams(email = email, recetaId =receta)
            repositoryReceta.deleteFavorito(params)
            }
    }

    fun agregarRecetaFavorito(email: String, receta: Int){
        viewModelScope.launch{
            var params = FavParams(email = email, recetaId =receta)
            repositoryReceta.addFavorito(params)}
    }


    fun syncRecetasLocales() {
        viewModelScope.launch {
            repositoryReceta.sincronizarPendientes()
        }
    }

    fun cargarRecetasSearch(termino: String) {
        viewModelScope.launch {
            val recetas = repositoryReceta.getBySearch(termino) // desde tu API
            recetasLiveData.postValue(recetas)
        }
    }

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


