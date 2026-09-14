package com.sap.codelab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sap.codelab.core.location.LocationReminderManager
import com.sap.codelab.core.repository.IMemoRepository
import com.sap.codelab.create.CreateMemoViewModel
import com.sap.codelab.detail.ViewMemoViewModel
import com.sap.codelab.home.HomeViewModel

internal class AppViewModelFactory(
    private val repository: IMemoRepository,
    private val locationReminderManager: LocationReminderManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when (modelClass) {
        HomeViewModel::class.java -> HomeViewModel(repository, locationReminderManager)
        CreateMemoViewModel::class.java -> CreateMemoViewModel(repository, locationReminderManager)
        ViewMemoViewModel::class.java -> ViewMemoViewModel(repository)
        else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    } as T
}
