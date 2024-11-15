package com.libertyinfosace.locationsource.ui.activity.place

import android.content.Context
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.libertyinfosace.locationsource.R
import com.libertyinfosace.locationsource.data.local.model.LocationDataEntity
import com.libertyinfosace.locationsource.databinding.ActivityAddedPlaceRouteDetailsScreenBinding
import com.libertyinfosace.locationsource.util.ExtraKey
import com.libertyinfosace.locationsource.util.getAddress
import com.libertyinfosace.locationsource.util.getDataFromIntent
import com.libertyinfosace.locationsource.util.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

class RouteDetailsScreenActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityAddedPlaceRouteDetailsScreenBinding
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var locationManager: LocationManager? = null
    private var googleMap: GoogleMap? = null
    private var midLatLng: LatLng? = null
    private var arrayOfAddedPlaceRouteLatLng: ArrayList<LocationDataEntity> ? = arrayListOf()
    private var arrayFields: List<Place.Field>? = null
    private var arrayCoordinates = arrayListOf<LatLng>()
    private var isListSortedToAsc: Boolean ? = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddedPlaceRouteDetailsScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        onClick()
    }

    private fun init() {
        if (!Places.isInitialized()) {
            Places.initialize(this@RouteDetailsScreenActivity, getString(R.string.google_map_key), Locale.US)
        }

        arrayFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
        getDataFromCallingScreen()
        mapInit()
    }

    private fun onClick() {
        with(binding) {
            ivBack.setOnClickListener {
                finish()
            }
        }
    }

    private fun getDataFromCallingScreen() {
        isListSortedToAsc = getDataFromIntent(intent,ExtraKey.EXTRA_LIST_SORTED_ASC).toBoolean()
        arrayOfAddedPlaceRouteLatLng = Gson().fromJson(getDataFromIntent(intent, ExtraKey.EXTRA_LIST), object : TypeToken<ArrayList<LocationDataEntity>>(){}.type)
       // Log.e("arrayOfAddedPlaceRouteLatLng>>>", "getDataFromCallingScreen: ${Gson().toJson(getDataFromIntent(intent, ExtraKey.EXTRA_LIST))}")
    }

    private fun mapInit() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this::onMapReady)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        this.googleMap?.uiSettings?.isZoomControlsEnabled= true
        this.googleMap?.uiSettings?.isScrollGesturesEnabled= true

        if (midLatLng != null) {
            midLatLng

        } else {
            LatLng(22.868876594753942, 79.34309786330815)
        }

        if (arrayOfAddedPlaceRouteLatLng.isNullOrEmpty().not()) {
            arrayOfAddedPlaceRouteLatLng?.forEach { location ->
                arrayCoordinates.add(LatLng(location.latitude?:0.0, location.longitude?:0.0))
            }

            if (arrayCoordinates?.isNullOrEmpty()?.not() == true) {
                drawRoadPath(googleMap, arrayCoordinates, getString(R.string.google_map_key))
            }
        }

    }

    private fun drawRoadPath(googleMap: GoogleMap?, sortedLocations: List<LatLng>, apiKey: String) {

        for ((index, location) in sortedLocations.withIndex()) {
            googleMap?.addMarker(MarkerOptions().position(location).title("Location$index: ${getAddress(location)}").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))
        }

        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(sortedLocations.first(), 10f))

        val waypoints = sortedLocations.drop(1).dropLast(1).joinToString("|") { "${it.latitude},${it.longitude}" }

        // URL for Directions API request
        val directionsUrl = StringBuilder("https://maps.googleapis.com/maps/api/directions/json?")
            .append("origin=${sortedLocations.first().latitude},${sortedLocations.first().longitude}")
            .append("&destination=${sortedLocations.last().latitude},${sortedLocations.last().longitude}")
            .append("&waypoints=$waypoints")
            .append("&key=$apiKey")
            .toString()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val jsonResult = fetchDirections(directionsUrl)
                val pathPoints = parseRoute(jsonResult)

                withContext(Dispatchers.Main) {
                    googleMap?.addPolyline(PolylineOptions().addAll(pathPoints).width(10f).color(R.color.app_primary))
                    val boundsBuilder = LatLngBounds.builder()
                    for (point in pathPoints) {
                        boundsBuilder.include(point)
                    }
                    googleMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100))

                }

            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    toast("Failed to fetch directions. Check your network connection.")
                }
                Log.e("DirectionsAPI", "Network error: ${e.localizedMessage}")

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    toast("An error occurred while processing directions.")
                }
                Log.e("DirectionsAPI", "Error parsing route: ${e.localizedMessage}")
            }
        }
    }

    // Fetch Directions API response as JSON with error handling
    @Throws(IOException::class)
    private fun fetchDirections(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 10000  // 10 seconds
        connection.readTimeout = 10000
        return connection.inputStream.bufferedReader().use { it.readText() }
    }

    // Parse JSON response to get route points with error handling
    @Throws(Exception::class)
    private fun parseRoute(jsonData: String): List<LatLng> {
        val jsonObject = JSONObject(jsonData)
        val pathPoints = mutableListOf<LatLng>()

        // Check for API response status
        val status = jsonObject.getString("status")
        if (status != "OK") {
            throw Exception("Directions API error: $status")
        }

        // Parsing steps in the route for road paths
        val routes = jsonObject.getJSONArray("routes")

        if (routes.length() > 0) {

            val legs = routes.getJSONObject(0).getJSONArray("legs")

            for (i in 0 until legs.length()) {

                val steps = legs.getJSONObject(i).getJSONArray("steps")

                for (j in 0 until steps.length()) {
                    val polyline = steps.getJSONObject(j).getJSONObject("polyline").getString("points")
                    pathPoints.addAll(decodePolyline(polyline))
                }
            }

        } else {
            throw Exception("No routes found in Directions API response.")
        }
        return pathPoints
    }

    // Decode polyline points to LatLng list
    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = mutableListOf<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dLat = if ((result and 1) != 0) (result shr 1).inv() else result shr 1
            lat += dLat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dLng = if ((result and 1) != 0) (result shr 1).inv() else result shr 1
            lng += dLng

            poly.add(LatLng(lat / 1E5, lng / 1E5))
        }

        return poly
    }

}