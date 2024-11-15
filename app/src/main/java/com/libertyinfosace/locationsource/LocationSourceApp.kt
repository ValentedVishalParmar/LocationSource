package com.libertyinfosace.locationsource

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.libertyinfosace.locationsource.data.local.database.roomdb.LocationSourcesDatabase
import java.lang.reflect.Modifier

class LocationSourceApp : Application() , Application.ActivityLifecycleCallbacks { companion object {

    var instance: LocationSourceApp? = null
    var currentActivity: Activity? = null
    var dbObject: LocationSourcesDatabase? = null
    val gson: Gson
        get() {
            return GsonBuilder().excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC).serializeNulls().setLenient().create()
        }

    fun executeTask(function: () -> Unit) {
        LocationSourcesDatabase.databaseWriteExecutor.execute {
            function()
        }
    }
}

    override fun onCreate() {
        super.onCreate()
        init()
    }

    private fun init() {
        instance = this@LocationSourceApp
        registerActivityLifecycleCallbacks(this)
        try {
            // Initialize Room database safely with applicationContext
            val database = LocationSourcesDatabase.getDatabase(applicationContext)
            dbObject =database
            Log.d("LocationSourceApp", "Database initialized successfully")
        } catch (e: Exception) {
            Log.e("LocationSourceApp", "Error initializing database: ${e.message}")
        }

    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
        currentActivity = p0
    }

    override fun onActivityStarted(p0: Activity) {
        currentActivity = p0
    }

    override fun onActivityResumed(p0: Activity) {
        currentActivity = p0
    }

    override fun onActivityPaused(p0: Activity) {
    }

    override fun onActivityStopped(p0: Activity) {
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {

    }

    override fun onActivityDestroyed(p0: Activity) {

    }

}