package com.libertyinfosace.locationsource.data.local.database.roomdb

import androidx.room.Dao
import androidx.room.Query
import com.libertyinfosace.locationsource.data.local.model.LocationDataEntity

@Dao
interface LocationDataDao : CommonDao<LocationDataEntity> {

    @Query("SELECT * FROM location_data WHERE id = :id")
    suspend fun getLocationDataById(id: String): LocationDataEntity

    @Query("SELECT * FROM location_data")
    suspend fun getAllDataOfLocation(): MutableList<LocationDataEntity>

    @Query("SELECT COUNT(id) FROM location_data")
    fun getAllCount(): Int

    @Query("DELETE FROM location_data")
    fun nukeTable()

}


