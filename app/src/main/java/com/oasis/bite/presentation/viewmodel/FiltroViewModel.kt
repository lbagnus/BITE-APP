package com.oasis.bite.presentation.viewmodel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FiltroViewModel : ViewModel() {
    val filtros = MutableLiveData<Filtro>()

    data class Filtro(
        val incluye: List<String>,
        val excluye: List<String>,
        val username: String? = null,
        val orderBy: String = "newest",
        val direction: String = "asc"   // "asc" o "desc"
    )
}