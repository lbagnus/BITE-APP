package com.oasis.bite.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oasis.bite.data.RetrofitInstance
import com.oasis.bite.data.model.CommentRequest
import com.oasis.bite.data.model.FavParams
import com.oasis.bite.data.repository.RecetaRepository
import com.oasis.bite.domain.models.Receta
import com.oasis.bite.domain.models.Ingrediente
import com.oasis.bite.domain.models.PasoReceta
import kotlinx.coroutines.launch

class RecetaViewModel : ViewModel() {

    private val apiService = RetrofitInstance.apiService

    private val repository = RecetaRepository(apiService)

    private val _receta = MutableLiveData<Receta?>()
    val receta: MutableLiveData<Receta?> get() = _receta

    private val _ingredientes = MutableLiveData<List<Ingrediente>>()
    val ingredientes: LiveData<List<Ingrediente>> get() = _ingredientes

    private val _pasos = MutableLiveData<List<PasoReceta>>()
    val pasos: LiveData<List<PasoReceta>> get() = _pasos
    val favoritoLiveData = MutableLiveData<List<Receta>?>()

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


}

