package com.libertyinfosace.locationsource.data.remote.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.libertyinfosace.locationsource.data.remote.repository.LocationDataRepository
import com.libertyinfosace.locationsource.viewmodel.LocationDataViewModel

class LocationDataViewModelFactory(private val repository: LocationDataRepository) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LocationDataViewModel(repository) as T
    }

}