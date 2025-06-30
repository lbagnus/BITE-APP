package com.oasis.bite.presentation.ui.home

import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oasis.bite.data.RetrofitInstance
import com.oasis.bite.data.model.FavParams
import com.oasis.bite.data.model.FavRequest
import com.oasis.bite.data.model.RecetaSearchParams
import com.oasis.bite.data.repository.RecetaRepository
import com.oasis.bite.domain.models.Category
import com.oasis.bite.domain.models.Receta
import com.oasis.bite.localdata.database.dao.RecetaDao
import kotlinx.coroutines.launch

class HomeViewModel( private val repositoryReceta: RecetaRepository) : ViewModel() {
    private val apiService = RetrofitInstance.apiService

    val recetasLiveData = MutableLiveData<List<Receta>?>()

    val categoryLiveData = MutableLiveData<List<Category>?>()

    val favoritoLiveData = MutableLiveData<List<Receta>?>()

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
            repositoryReceta.deleteFavorito(params)}
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
    
}


