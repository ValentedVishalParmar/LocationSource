package com.libertyinfosace.locationsource.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.libertyinfosace.locationsource.data.local.model.LocationDataEntity
import com.libertyinfosace.locationsource.data.local.model.RequestResponse
import com.libertyinfosace.locationsource.data.remote.repository.LocationDataRepository
import com.libertyinfosace.locationsource.util.Constants.ERR_SOMETHING_WENT_WRONG
import com.libertyinfosace.locationsource.util.Coroutines

class LocationDataViewModel(private val repository: LocationDataRepository) : ViewModel() {

    private val setDatabaseException = MutableLiveData<RequestResponse?>()
    var setAllDataOfLocationFromDB = MutableLiveData<MutableList<LocationDataEntity>?>()
    val setInsertStatusLocationDataListIntoDB = MutableLiveData<Boolean?>()
    val locationDataDetails = MutableLiveData<LocationDataEntity?>()
    var isLoading = MutableLiveData<Boolean>().apply { value = false }
    var intEditPosition = -1
    var intCurrentPage = 0
    var intLocationDataListTotalIntoDB = 0
    var isListSortedToDescending: Boolean = true
    var isEdit: Boolean = false

    fun insertLocationData(locationData: LocationDataEntity) {
        Coroutines.ioThenMain({ repository.insertLocationData(locationData) }, { response ->
            setInsertStatusLocationDataListIntoDB.value = response

        }, { error ->
            setDatabaseException.value = RequestResponse(message = ERR_SOMETHING_WENT_WRONG)
            Log.e("EXCEPTION", "Error occurred insertLocationData(): ${error.message}")
        })
    }

    fun updateLocationData(locationData: LocationDataEntity) {
        Coroutines.ioThenMain({ repository.updateLocationData(locationData) }, { response ->
        }, { error ->
            setDatabaseException.value = RequestResponse(message = ERR_SOMETHING_WENT_WRONG)
            Log.e("EXCEPTION", "Error occurred insertLocationData(): ${error.message}")
        })
    }

    fun deleteLocationData(locationData: LocationDataEntity) {
        Coroutines.ioThenMain({ repository.deleteLocationData(locationData) }, { response ->
        }, { error ->
            setDatabaseException.value = RequestResponse(message = ERR_SOMETHING_WENT_WRONG)
            Log.e("EXCEPTION", "Error occurred insertLocationData(): ${error.message}")
        })
    }

    fun fetchAllDataOfLocationFromDB() {
        if (isLoading.value == true) return
        isLoading.value = true

        Coroutines.ioThenMain({
            isListSortedToDescending = !isListSortedToDescending

            repository.getPaginatedLocationData()

        }, { response ->
            if (response?.success == true) {
                response.data?.let {
                    setAllDataOfLocationFromDB.value = response.data as MutableList<LocationDataEntity>?
                }
            }

            isLoading.value = false

        }, { error ->
            setDatabaseException.value = RequestResponse(message = ERR_SOMETHING_WENT_WRONG)
            Log.e("EXCEPTION", "Error occurred fetchAllDataOfLocationFromDB(): ${error.message}")
        })
    }

    fun getLocationDataCount() {

        Coroutines.ioThenMain({

            repository.getLocationDataCount()
        }, { response ->
            if (response != null) {
                intLocationDataListTotalIntoDB = response
            }

        }, { error ->
            setDatabaseException.value = RequestResponse(message = ERR_SOMETHING_WENT_WRONG)
            Log.e("EXCEPTION", "Error occurred getLocationDataCount(): ${error.message}")
        })
    }

    val getInsertionStatusOfLocationDataFromDB: MutableLiveData<Boolean?> get() = setInsertStatusLocationDataListIntoDB
    val getAllDataOfLocationDataFromDB: MutableLiveData<MutableList<LocationDataEntity>?> get() = setAllDataOfLocationFromDB
    val getDatabaseException: MutableLiveData<RequestResponse?> get() = setDatabaseException
}