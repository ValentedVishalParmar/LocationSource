package com.libertyinfosace.locationsource.ui.activity.place

import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.libertyinfosace.locationsource.R
import com.libertyinfosace.locationsource.databinding.ActivityAddLocationPlaceBinding
import com.libertyinfosace.locationsource.util.Constants
import com.libertyinfosace.locationsource.util.ExtraKey
import com.libertyinfosace.locationsource.util.getAddress
import com.libertyinfosace.locationsource.util.getDataFromIntent
import com.libertyinfosace.locationsource.util.getLocationComponentsFromAddress
import java.util.Locale

class AddPlaceScreenActivity : AppCompatActivity() , OnMapReadyCallback {

    private lateinit var binding: ActivityAddLocationPlaceBinding
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var locationManager: LocationManager? = null
    private var googleMap: GoogleMap? = null
    private var address = ""
    private var midLatLng: LatLng? = null
    private var selectedPlace: Place ? = null
    private var arrayCoordinates = arrayListOf<Double>()
    private var arrayFields: List<Place.Field>? = null
    private var isEdit: Boolean ? = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddLocationPlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        onClick()

    }

    private fun init() {
        if (!Places.isInitialized()) {
            Places.initialize(this@AddPlaceScreenActivity, getString(R.string.google_map_key), Locale.US)
        }
        arrayFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
        getDataFromCallingScreenIntent()
        mapInit()
    }

    private fun getDataFromCallingScreenIntent() {
        if (getDataFromIntent(intent,ExtraKey.EXTRA_IS_EDIT).toBoolean()) {
            isEdit = getDataFromIntent(intent,ExtraKey.EXTRA_IS_EDIT).toBoolean()
            midLatLng = LatLng(getDataFromIntent(intent,ExtraKey.EXTRA_LATITUDE)?.toDouble() ?: 0.0, getDataFromIntent(intent,ExtraKey.EXTRA_LONGITUDE)?.toDouble() ?: 0.0)
        }
    }

    private fun onClick() {
        with(binding) {
            ivBack.setOnClickListener {
                finish()
            }

            searchEditText.setOnClickListener {
                startAutoCompleteAddressDialog()
            }

            searchEditText.doOnTextChanged { text, start, before, count ->
                if (count>=1) {
                    startAutoCompleteAddressDialog()
                }
            }

            btnCancel.setOnClickListener {
                finish()
            }

            btnSelectAddress.setOnClickListener {
                address = "${midLatLng?.let { it1 -> getAddress(it1) }}"
                if (address.isNotEmpty()) {
                    val name = getLocationComponentsFromAddress(this@AddPlaceScreenActivity, address)

                    val intent = Intent()
                    intent.putExtra(ExtraKey.EXTRA_ADDRESS, address)
                    intent.putExtra(ExtraKey.EXTRA_ADDRESS_NAME, name)
                    intent.putExtra(ExtraKey.EXTRA_LATITUDE, midLatLng?.latitude.toString())
                    intent.putExtra(ExtraKey.EXTRA_LONGITUDE, midLatLng?.longitude.toString())
                    intent.putExtra(ExtraKey.EXTRA_IS_EDIT, isEdit.toString())
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
        }
    }

    private fun startAutoCompleteAddressDialog() {
        val intent: Intent? = arrayFields?.let { arrayFields ->
            Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, arrayFields).build(this@AddPlaceScreenActivity)
        }

        if (intent != null) {
            autoCompleteRequestCodeContract.launch(intent)
        }
    }

    private val autoCompleteRequestCodeContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        when (result.resultCode) {
            RESULT_OK -> {
                arrayCoordinates.clear()
                binding.searchEditText.text?.clear()
                binding.searchEditText.clearFocus()

                selectedPlace = Autocomplete.getPlaceFromIntent(result.data!!)
                selectedPlace?.latLng?.let { arrayCoordinates.add(it.longitude) }
                selectedPlace?.latLng?.let { arrayCoordinates.add(it.latitude) }

                googleMap?.clear()
                addMarkerWithZoomOnSelectedPlace(LatLng(arrayCoordinates[1], arrayCoordinates[0]))
                }

            AutocompleteActivity.RESULT_ERROR -> {
                Autocomplete.getStatusFromIntent(result.data!!)
            }

            RESULT_CANCELED -> {}
        }
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

        var latLng: LatLng? = null

        latLng = if (midLatLng != null) {
            midLatLng

        } else {
            LatLng(22.868876594753942, 79.34309786330815)
        }

       addMarkerWithZoomOnSelectedPlace(latLng)

        googleMap.setOnMapClickListener {
            googleMap.clear()
            googleMap.addMarker(MarkerOptions().position(it))
            midLatLng = LatLng(it.latitude, it.longitude)

        }
    }

    private fun addMarkerWithZoomOnSelectedPlace(latLng: LatLng?) {
        midLatLng = latLng
        latLng?.let { CameraUpdateFactory.newLatLng(it) }?.let { googleMap?.moveCamera(it) }
        latLng?.let { CameraUpdateFactory.newLatLngZoom(it, 10f) }?.let { googleMap?.animateCamera(it) }
        latLng?.let { MarkerOptions().position(it) }?.let { googleMap?.addMarker(it) }
    }

}