package com.libertyinfosace.locationsource.ui.activity.home

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.model.LatLng
import com.libertyinfosace.locationsource.LocationSourceApp
import com.libertyinfosace.locationsource.R
import com.libertyinfosace.locationsource.data.local.model.LocationDataEntity
import com.libertyinfosace.locationsource.data.remote.factory.LocationDataViewModelFactory
import com.libertyinfosace.locationsource.data.remote.repository.LocationDataRepository
import com.libertyinfosace.locationsource.databinding.ActivityLocationSourceBinding
import com.libertyinfosace.locationsource.interfaces.OnItemClickCListener
import com.libertyinfosace.locationsource.ui.activity.place.AddPlaceScreenActivity
import com.libertyinfosace.locationsource.ui.activity.place.RouteDetailsScreenActivity
import com.libertyinfosace.locationsource.ui.adapter.LocationAdapter
import com.libertyinfosace.locationsource.util.ExtraKey
import com.libertyinfosace.locationsource.util.calculateDistance
import com.libertyinfosace.locationsource.util.checkLocationPermission
import com.libertyinfosace.locationsource.util.getDataFromIntent
import com.libertyinfosace.locationsource.util.handleOnBackPressed
import com.libertyinfosace.locationsource.util.hideLoader
import com.libertyinfosace.locationsource.util.navigateTo
import com.libertyinfosace.locationsource.util.openAppSettings
import com.libertyinfosace.locationsource.util.showLoader
import com.libertyinfosace.locationsource.util.toast
import com.libertyinfosace.locationsource.util.viewGone
import com.libertyinfosace.locationsource.util.viewVisible
import com.libertyinfosace.locationsource.viewmodel.LocationDataViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

class LocationSourceScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLocationSourceBinding
    private lateinit var locationDataViewModel: LocationDataViewModel
    private var locationAdapter: LocationAdapter? = null
    private var arrayLocationData: ArrayList<LocationDataEntity>? = arrayListOf()
    private lateinit var arrayPermission: Array<String>
    private var isBackButtonPressedToExitFromApp = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationSourceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        onClicks()
        setAdapter()
        setObserver()
        getLocationDataCountFromDB()
    }

    private fun init() {
        arrayPermission = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        locationDataViewModel = ViewModelProvider(this@LocationSourceScreenActivity, LocationDataViewModelFactory(LocationDataRepository()))[LocationDataViewModel::class.java]

    }

    private fun onClicks() {
        with(binding) {

            btnAddLocation.setOnClickListener {
                navigateToAddNewPlaceScreen()
            }

            btnAddMoreLocation.setOnClickListener {
                navigateToAddNewPlaceScreen()
            }


            ivSort.setOnClickListener {
                if (arrayLocationData.isNullOrEmpty().not()) {
                    if (locationDataViewModel.isListSortedToDescending) {
                        //val ascendingList = arrayLocationData?.sortedByDescending { it.distance }?.asReversed()?.toCollection(ArrayList())
                        val ascendingList = sortLocationsByDistance(arrayLocationData?: arrayListOf()).toCollection(ArrayList())
                        locationDataViewModel.isListSortedToDescending = false
                        locationAdapter?.updateNewList(ascendingList)

                    } else {
                        //val descendingList = arrayLocationData?.sortedByDescending { it.distance }?.toCollection(ArrayList())
                        val descendingList = sortLocationsByDistance(arrayLocationData?: arrayListOf(), false).toCollection(ArrayList())
                        locationDataViewModel.isListSortedToDescending = true
                        locationAdapter?.updateNewList(descendingList)
                    }
                }
            }

            ivApp.setOnClickListener {
                toast("This feature Under development")
            }

            ivDirectionLogo.setOnClickListener {
                if (arrayLocationData?.size?.let { listSize -> listSize >= 2 } == true) {
                    navigateTo(RouteDetailsScreenActivity::class.java, {
                        this.putString(
                            ExtraKey.EXTRA_LIST,
                            LocationSourceApp.gson.toJson(arrayLocationData)
                        )
                        this.putString(
                            ExtraKey.EXTRA_LIST_SORTED_ASC,
                            (!locationDataViewModel.isListSortedToDescending).toString()
                        )
                    })
                }
            }

        }

        manageBackPressEvent()
    }

    private fun requestLocationPermission() {
        launchLocationPermission.launch(arrayPermission)
    }

    private fun setAdapter() {
        binding.rvLocation.apply {
            layoutManager = LinearLayoutManager(this@LocationSourceScreenActivity, LinearLayoutManager.VERTICAL, false)
            locationAdapter = LocationAdapter(this@LocationSourceScreenActivity, arrayLocationData ?: arrayListOf(), object : OnItemClickCListener {

                    override fun onUpdateLocation(position: Int) {
                        super.onUpdateLocation(position)

                        if (position >= 0) {
                            locationDataViewModel.isEdit = true
                            locationDataViewModel.intEditPosition = position
                            val locationData = arrayLocationData?.get(position)
                            navigateToAddNewPlaceScreen(LatLng(locationData?.latitude ?: 0.0, locationData?.longitude ?: 0.0))
                        }
                    }

                    override fun onDeleteLocation(position: Int) {
                        super.onDeleteLocation(position)
                        if (position >= 0) {
                            if (arrayLocationData?.isNullOrEmpty()?.not() == true) {
                                showLocationDeleteConfirmationDialog(position)
                            }
                        }
                    }
                }
            )
            adapter = locationAdapter
        }
    }

    private fun getLocationDataCountFromDB() {
        locationDataViewModel.getLocationDataCount()
    }

    private fun navigateToAddNewPlaceScreen(latLong: LatLng = LatLng(0.0, 0.0)) {
        val intent = Intent(this@LocationSourceScreenActivity, AddPlaceScreenActivity::class.java)

        if (latLong.latitude != 0.0 && latLong.longitude != 0.0) {
            intent.putExtra(ExtraKey.EXTRA_LONGITUDE, latLong.longitude.toString())
            intent.putExtra(ExtraKey.EXTRA_LATITUDE, latLong.latitude.toString())
            intent.putExtra(ExtraKey.EXTRA_IS_EDIT, locationDataViewModel.isEdit.toString())

        } else {
            locationDataViewModel.isEdit = false
            intent.putExtra(ExtraKey.EXTRA_IS_EDIT, locationDataViewModel.isEdit.toString())
        }

        launcherAddEditPlaceLocationAddress.launch(intent)
    }

    private fun setObserver() {

        locationDataViewModel.getDatabaseException.observe(this) { databaseException ->
            hideLoader()
            toast(databaseException?.message)
        }

        locationDataViewModel.getInsertionStatusOfLocationDataFromDB.observe(this) { isSuccess ->
            if (isSuccess == true) {
                fetchAllDataOfLocationFromDB()
            }
        }

        locationDataViewModel.getAllDataOfLocationDataFromDB.observe(this) { arrayListLocationData ->
            val newListOfLocationData = ArrayList<LocationDataEntity>()
            // Log.e("getAllDataOfLocationDataFromDB>>>", "arrayListLocationData: ${arrayListLocationData?.size}")

            arrayListLocationData?.forEach { locationData ->
                newListOfLocationData.add(locationData)
            }

            manageArrayListOfLocationData(newListOfLocationData)
        }

        locationDataViewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                showLoader()

            } else {
                hideLoader()
            }
        }
    }

    private fun manageArrayListOfLocationData(arrayListLocationData: ArrayList<LocationDataEntity>?) {
        arrayListLocationData?.takeIf { it.isNotEmpty() }?.let { locationDataList ->
            if (locationAdapter == null) {
                arrayLocationData?.clear()
                arrayLocationData?.addAll(locationDataList)
                setAdapter()

            } else {
                locationAdapter?.appendProducts(locationDataList)
                toggleInfoViewAndLocationListView(arrayLocationData?.isEmpty() == true)
            }

        } ?: {
            if (checkLocationPermission()) {
                fetchAllDataOfLocationFromDB()

            } else {
                requestLocationPermission()
            }
        }

    }

    private val launchLocationPermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var allGranted = false

            permissions.entries.forEach {
                val isGranted = it.value
                allGranted = isGranted
            }

            if (allGranted) {
                fetchAllDataOfLocationFromDB()

            } else {
                if (checkLocationPermission().not()) {
                    showLocationDialog()
                }
            }
        }

    private val launcherAddEditPlaceLocationAddress = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == RESULT_OK) {

                try {

                if (result.data?.hasExtra(ExtraKey.EXTRA_ADDRESS) == true) {

                    val name = getDataFromIntent(result.data, ExtraKey.EXTRA_ADDRESS_NAME)
                    val address = getDataFromIntent(result.data, ExtraKey.EXTRA_ADDRESS)
                    val latitude = getDataFromIntent(result.data, ExtraKey.EXTRA_LATITUDE)
                    val longitude = getDataFromIntent(result.data, ExtraKey.EXTRA_LONGITUDE)
                    val isEdit = getDataFromIntent(result.data, ExtraKey.EXTRA_IS_EDIT)
                    val locationDataEntity = LocationDataEntity()

                    name?.let { nameValue ->
                        locationDataEntity.name = nameValue
                    }

                    longitude?.let { lng ->
                        locationDataEntity.longitude = lng.toDouble()
                    }

                    latitude?.let { lat ->
                        locationDataEntity.latitude = lat.toDouble()
                    }

                    locationDataEntity.isPrimary = arrayLocationData.isNullOrEmpty()

                    if (arrayLocationData.isNullOrEmpty().not()) {
                        locationDataEntity.distance = calculateDistances(
                            primaryLatLng = LatLng(arrayLocationData?.get(0)?.latitude ?: 0.0, arrayLocationData?.get(0)?.longitude ?: 0.0), location = LatLng(locationDataEntity.latitude ?: 0.0, locationDataEntity.longitude ?: 0.0))
                    }

                    if (address?.isNotEmpty() == true) {
                        locationDataEntity.address = address
                    }

                    if (isEdit.toBoolean()) {
                        locationDataViewModel.updateLocationData(locationDataEntity)

                        if (locationDataViewModel.intEditPosition != -1) {
                            arrayLocationData?.set(locationDataViewModel.intEditPosition, locationDataEntity)
                            locationAdapter?.notifyItemChanged(locationDataViewModel.intEditPosition, locationDataEntity)
                        }

                    } else {
                        locationDataViewModel.insertLocationData(locationDataEntity)
                    }

                } else {
                    Log.e("launcherGetPropertyAddressMAP>>>", "null ")
                }

                } catch(e:Exception) {
                    Log.e("Exception>>>", "${e.message} ")
                }
            }
        }

    private fun showLocationDialog() {
        val msg: String = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            getString(R.string.text_location_permission)

        } else {
            getString(R.string.text_location_permission_all_time)
        }

        AlertDialog.Builder(this@LocationSourceScreenActivity)
            .setTitle(R.string.title_location_permission).setMessage(msg)
            .setPositiveButton(getString(R.string.open_setting)) { _, _ ->
                openAppSettings()
            }.create().show()
    }

    private fun showLocationDeleteConfirmationDialog(position:Int) {
        if (position >= 0) {
        AlertDialog.Builder(this@LocationSourceScreenActivity)
                .setTitle(R.string.delete).setMessage(getString(R.string.delete_message))
                .setPositiveButton(getString(R.string.delete)) { _, _ ->
                    arrayLocationData?.get(position)?.let { locationDataViewModel.deleteLocationData(it) }
                    arrayLocationData?.removeAt(position)
                    locationAdapter?.updateNewList(arrayLocationData)
                    toggleInfoViewAndLocationListView(arrayLocationData.isNullOrEmpty())
                }.create().show()
        }
    }

    private fun manageBackPressEvent() {
        handleOnBackPressed {
            askForTapBackButtonAgainToExitApp()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun askForTapBackButtonAgainToExitApp() {
        if (isBackButtonPressedToExitFromApp) {
            finishAffinity()
        }

        isBackButtonPressedToExitFromApp = true

        GlobalScope.launch {
            delay(2000)
            isBackButtonPressedToExitFromApp = false
        }
    }

    override fun onResume() {
        super.onResume()

        try {
            if (checkLocationPermission().not()) {
                requestLocationPermission()

            } else {
                fetchAllDataOfLocationFromDB()
            }

            toggleInfoViewAndLocationListView(arrayLocationData?.isEmpty() == true)

        } catch (e: Exception) {
            Log.e("onResumeError", "Error in onResume: ${e.message}")
        }
    }


    private fun fetchAllDataOfLocationFromDB() {
        locationDataViewModel.fetchAllDataOfLocationFromDB()
    }

    private fun toggleInfoViewAndLocationListView(isInfoVisible: Boolean = true) {
        with(binding) {

            if (isInfoVisible || arrayLocationData.isNullOrEmpty()) {
                viewVisible(clInfoView)
                viewGone(ivDirectionLogo)
                viewGone(ivSort)
                viewGone(clLocationViews)

            } else {
                viewGone(clInfoView)
                if (arrayLocationData?.size?.let { size -> size >= 2 } == true) {
                    viewVisible(ivDirectionLogo)
                    viewVisible(ivSort)

                } else {
                    viewGone(ivDirectionLogo)
                    viewGone(ivSort)
                }

                viewVisible(clLocationViews)
            }
        }
    }

    private fun calculateDistances(primaryLatLng: LatLng?, location: LatLng?): Double {
        if (primaryLatLng == null || location == null) return 0.0

        val primaryLocation = Location("primary").apply {
            latitude = primaryLatLng.latitude
            longitude = primaryLatLng.longitude
        }

        val targetLocation = Location("target").apply {
            latitude = location.latitude
            longitude = location.longitude
        }

        val distanceInMeters = primaryLocation.distanceTo(targetLocation)
        val distances = distanceInMeters / 1000.0
        return BigDecimal(distances).setScale(2, RoundingMode.HALF_UP).toDouble()
    }

    private fun sortLocationsByDistance(locations: List<LocationDataEntity>, sortAscending: Boolean = true): List<LocationDataEntity> {
        if (locations.isEmpty()) return emptyList()

        val primaryLocation = locations.firstOrNull { it.isPrimary == true }
        val sortedList = locations.map { location ->

            if (location.isPrimary == true) {
                location

            } else {
                location.copy(
                    distance = primaryLocation?.latitude?.let {

                        location.latitude?.let { it1 ->

                            primaryLocation?.longitude?.let { it2 ->

                                location.longitude?.let { it3 ->
                                    calculateDistance(primaryLat = it, primaryLng = it2, targetLat = it1, targetLng = it3)
                                }
                            }
                        }
                    }
                )
            }

        }.sortedWith(compareBy({ it.isPrimary?.not() == true }, { it.distance })).let { if (sortAscending) it else it.reversed() }

        return sortedList
    }

}