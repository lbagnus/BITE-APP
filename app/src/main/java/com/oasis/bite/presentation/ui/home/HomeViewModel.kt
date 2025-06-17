package com.oasis.bite.presentation.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oasis.bite.data.RetrofitInstance
import com.oasis.bite.data.model.RecetaSearchParams
import com.oasis.bite.data.repository.RecetaRepository
import com.oasis.bite.domain.models.Category
import com.oasis.bite.domain.models.Receta
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val apiService = RetrofitInstance.apiService

    private val repositoryReceta = RecetaRepository(apiService)


    val recetasLiveData = MutableLiveData<List<Receta>?>()

    val categoryLiveData = MutableLiveData<List<Category>?>()

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

}