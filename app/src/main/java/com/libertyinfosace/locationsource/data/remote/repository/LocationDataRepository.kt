package com.libertyinfosace.locationsource.data.remote.repository

import com.libertyinfosace.locationsource.LocationSourceApp
import com.libertyinfosace.locationsource.data.local.model.LocationDataEntity
import com.libertyinfosace.locationsource.data.local.model.RequestResponseData

class LocationDataRepository() : RequestResponseData()  {
    private val locationDao = LocationSourceApp.dbObject?.locationDao()

    fun insertLocationData(locationData: LocationDataEntity): Boolean {
        var isInserted = false
        locationDao?.insert(locationData)?.let { long ->
            isInserted= long != 0L
        }
        return isInserted

    }

    fun updateLocationData(locationData: LocationDataEntity) {
        locationDao?.update(locationData)
    }

    fun deleteLocationData(locationData: LocationDataEntity) {
        locationDao?.delete(locationData)
    }

    suspend fun getPaginatedLocationData()= requestData {
        locationDao?.getAllDataOfLocation()
    }

     fun getLocationDataCount(): Int? {
        return locationDao?.getAllCount()
    }
}