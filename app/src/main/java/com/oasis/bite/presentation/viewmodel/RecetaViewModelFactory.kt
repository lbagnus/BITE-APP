package com.oasis.bite.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.oasis.bite.data.RetrofitInstance
import com.oasis.bite.data.repository.RecetaRepository
import com.oasis.bite.localdata.database.DatabaseProvider
import com.oasis.bite.presentation.ui.home.HomeViewModel

class RecetaViewModelFactory( private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecetaViewModel::class.java)) {
            val apiService = RetrofitInstance.apiService
            val db = DatabaseProvider.getDatabase(context)
            val recetaDao = db.recetaDao()
            val repository = RecetaRepository(apiService, recetaDao, context)

            @Suppress("UNCHECKED_CAST")
            return RecetaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}