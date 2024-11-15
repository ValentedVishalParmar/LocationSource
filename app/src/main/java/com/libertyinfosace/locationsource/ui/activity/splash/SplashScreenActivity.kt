package com.libertyinfosace.locationsource.ui.activity.splash

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.libertyinfosace.locationsource.LocationSourceApp
import com.libertyinfosace.locationsource.databinding.ActivitySplashScreenBinding
import com.libertyinfosace.locationsource.ui.activity.home.LocationSourceScreenActivity
import com.libertyinfosace.locationsource.util.Constants
import com.libertyinfosace.locationsource.util.finishAndNavigateTo

class SplashScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding
    private var handler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        init()
        setSplashScreenData()
    }

    private fun init() {
        handler = Handler(Looper.getMainLooper())
        LocationSourceApp.currentActivity = this@SplashScreenActivity
    }

    private fun setSplashScreenData() {
        handler?.postDelayed({
            finishAndNavigateTo(LocationSourceScreenActivity::class.java)
        }, Constants.SPLASH_SCREEN_DELAY * 1000)
    }


    override fun onDestroy() {
        super.onDestroy()
        if (this@SplashScreenActivity::binding.isInitialized) {
            handler = null
        }
    }
}