package com.libertyinfosace.locationsource.util

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.libertyinfosace.locationsource.data.local.model.LocationDataEntity

object Converters {

    @TypeConverter
    fun fromLocationDataList(value: List<LocationDataEntity>?): String? {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toLocationDataList(value: String?): List<LocationDataEntity>? {
        val listType = object : TypeToken<List<LocationDataEntity>>() {}.type
        return Gson().fromJson(value, listType)
    }

  }
