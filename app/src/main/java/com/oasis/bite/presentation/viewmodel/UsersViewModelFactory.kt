package com.oasis.bite.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.oasis.bite.localdata.repository.UserSessionRepository
import com.oasis.bite.presentation.viewmodel.UsersViewModel


class UsersViewModelFactory(private val context: Context): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UsersViewModel::class.java)) {
            val sesssionRepo = UserSessionRepository.getInstance(context.applicationContext)
            return UsersViewModel(sesssionRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}