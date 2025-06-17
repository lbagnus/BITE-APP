package com.oasis.bite.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oasis.bite.data.RetrofitInstance
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

}

