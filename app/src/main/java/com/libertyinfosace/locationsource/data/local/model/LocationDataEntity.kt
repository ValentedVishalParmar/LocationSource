package com.libertyinfosace.locationsource.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "location_data")
data class LocationDataEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var name: String? = null,
    var address: String? = null,
    var distance: Double? = null,
    var latitude: Double ? = null,
    var longitude: Double? = null,
    var isPrimary: Boolean? = null,
    val createdAt: String? = null,
    val updateAt: String? = null
) : Serializable
