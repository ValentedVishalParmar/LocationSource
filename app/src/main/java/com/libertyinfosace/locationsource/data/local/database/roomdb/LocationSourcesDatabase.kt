package com.libertyinfosace.locationsource.data.local.database.roomdb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.libertyinfosace.locationsource.data.local.model.LocationDataEntity
import com.libertyinfosace.locationsource.util.Converters
import com.libertyinfosace.locationsource.util.ioThread
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Database(entities = [LocationDataEntity::class], version = 1)
@TypeConverters(value = [Converters::class])
abstract class LocationSourcesDatabase : RoomDatabase() {

    abstract fun locationDao(): LocationDataDao

    companion object {
        private const val NUMBER_OF_THREADS = 4
        val databaseWriteExecutor: ExecutorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS)

        @Volatile
        private var INSTANCE: LocationSourcesDatabase? = null

        fun getDatabase(context: Context?): LocationSourcesDatabase? {

                if (INSTANCE == null) {
                    synchronized(LocationSourcesDatabase::class.java) {
                        if (INSTANCE == null) {

                              context?.let {
                             val intence =    Room.databaseBuilder(it, LocationSourcesDatabase::class.java, "location_source_app_db.db")
                                 .allowMainThreadQueries()
                                 .fallbackToDestructiveMigration()
                                 .addCallback(object : Callback() {

                                     override fun onCreate(db: SupportSQLiteDatabase) {
                                         super.onCreate(db)
                                         ioThread {
                                             initializeFreshDatabase()
                                         }
                                     }

                                 }).build()

                                 INSTANCE = intence
                             }
                        }
                    }
                }
                return INSTANCE
            }


        fun initializeFreshDatabase() {

        }
    }

}
