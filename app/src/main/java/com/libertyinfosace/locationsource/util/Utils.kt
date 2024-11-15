package com.libertyinfosace.locationsource.util

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.libertyinfosace.locationsource.BuildConfig
import com.libertyinfosace.locationsource.R
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

fun <T> Context.navigateTo(destinationScreen: Class<T>, extras: Bundle.() -> Unit = {}) {
    val intent = Intent(this, destinationScreen)
    intent.putExtras(Bundle().apply(extras))
    startActivity(intent)
}

fun <T> Context.finishAndNavigateTo(destinationScreen: Class<T>, extras: Bundle.() -> Unit = {}) {
    val intent = Intent(this, destinationScreen)
    intent.putExtras(Bundle().apply(extras))
    startActivity(intent)
    (this as Activity).finish()
}

fun <T> getData(data: Any?, aClass: Class<T>): T? {
    data?.let {
        if (data is String) {
            return Gson().fromJson(data, aClass)

        } else {
            return Gson().fromJson(Gson().toJson(data), aClass)
        }
    }

    return null
}

fun getDataFromIntent(intent: Intent?, data: String): String? {
    return intent?.extras?.getString(data)
}



fun AppCompatActivity.handleOnBackPressed(onBackPressed: () -> Unit = { finish() }) {
    onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            onBackPressed.invoke()
        }
    })
}

fun Activity.disableClick(view: View?) {

    runOnUiThread {
        view?.isClickable = false
        Handler(Looper.getMainLooper()).postDelayed({
            if (this.isFinishing.not()) {
                view?.isClickable = true
            }
        }, 1000)
    }

}

fun Double?.FormattedAmount(): String {

    val otherSymbols = DecimalFormatSymbols(Locale.US)
    val decimalFormat = DecimalFormat("#,###,###.##", otherSymbols)
    return decimalFormat.format(this).toString()

}

val Context.isNetworkConnected: Boolean
    get() {
        val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return manager.getNetworkCapabilities(manager.activeNetwork)?.let {
            it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || it.hasTransport(
                NetworkCapabilities.TRANSPORT_CELLULAR
            ) || it.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) || it.hasTransport(
                NetworkCapabilities.TRANSPORT_ETHERNET
            ) || it.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
        } ?: false
    }

fun Context.checkLocationPermission(): Boolean {
    return ((ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED))
}

//OPEN APP SETTINGS
fun Activity.openAppSettings() {
    val intent = Intent()
    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
    val uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
    intent.data = uri
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    startActivity(intent)
}

fun Activity.toast(msg: String?) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun isEmpty(editText: EditText?): Boolean {
    return editText?.text?.trim()?.toString().isNullOrEmpty()
}

fun TextView?.getText(): String? {
    return this?.text?.trim()?.toString()
}

fun viewGone(view: View) {
    view.visibility = View.GONE
}

fun viewVisible(view: View) {
    view.visibility = View.VISIBLE
}

fun viewInVisible(view: View) {
    view.visibility = View.INVISIBLE
}

//CURSOR TO END
fun EditText.cursorToEnd() {
    this.requestFocus()
    this.setSelection(this.length())
}

fun setText(textView: TextView?, id: Int?) {
    if (id != null) {
        textView?.setText(id)
    }
}

fun setText(textView: AppCompatTextView?, id: Int?) {
    if (id != null) {
        textView?.setText(id)
    }
}

fun setText(btn: Button?, id: Int?) {
    if (id != null) {
        btn?.setText(id)
    }
}

fun setText(textView: TextView?, text: String?) {
    textView?.text = text
}

fun setText(btn: Button?, text: String?) {
    btn?.text = text
}

inline var TextView.strike: Boolean
    set(visible) {
        paintFlags = if (visible) paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        else paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
    }
    get() = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG == Paint.STRIKE_THRU_TEXT_FLAG

fun Activity.getDrawable(drawableImage: Int): Drawable? {
    return ContextCompat.getDrawable(this, drawableImage)
}


fun loadCircleImage(
    imageView: ImageView,
    imagePath: String,
    placeHolder: Drawable? = null
) {
    imagePath.let {
        Glide.with(imageView.context)
            .load(it)
            .apply(
                RequestOptions().transform(
                    CircleCrop(),
                    RoundedCorners(16)
                )
            )
            .placeholder(placeHolder)
            .into(imageView)

    }
}

fun loadCircleImage(imageView: ImageView, imagePath: String) {
    imagePath.let {
        Glide.with(imageView.context)
            .load(it)
            .apply(
                RequestOptions().transform(
                    CircleCrop(),
                    RoundedCorners(16)
                )
            )
            .into(imageView)

    }
}

fun loadCircleImageProfile(imageView: ImageView, placeHolder: Drawable?) {

    Glide.with(imageView.context)
        .load(placeHolder)
        .apply(
            RequestOptions().transform(
                CircleCrop(),
                RoundedCorners(16)
            )
        )
        .into(imageView)


}

fun loadImage(imageView: ImageView, imagePath: String?, placeHolder: Drawable? = null) {
    try {
        imagePath?.let {
            Glide.with(imageView.context)
                .load(it)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(false)
                .placeholder(placeHolder)
                .into(imageView)
        } ?: {
            Glide.with(imageView.context)
                .load(placeHolder)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(false)
                .into(imageView)
        }
    } catch (e: Exception) {
        Log.e("imageLoadError-->", "${e.message}")
    }
}

//SHOW LOADER
var dialog: Dialog? = null
fun Activity.showLoader(): Dialog? {
    dialog = Dialog(
        this,
        R.style.TransparentProgressDialog
    ) // Make the dialog cancelable so it responds to back button

    dialog?.setCancelable(false)

    if (this.isFinishing.not() && dialog?.isShowing?.not() == true) {
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setContentView(R.layout.dialog_progressbar)
        val progressBar: ProgressBar? = dialog?.findViewById(R.id.progressBar)
        val rlDialogProgressbar: RelativeLayout? = dialog?.findViewById(R.id.rlDialogProgressbar)

        rlDialogProgressbar?.tag = 0
        rlDialogProgressbar?.postDelayed({ }, 100)

        progressBar?.indeterminateDrawable?.setTint(ContextCompat.getColor(this, R.color.black))
        progressBar?.indeterminateDrawable?.setTintMode(PorterDuff.Mode.SRC_ATOP)
        progressBar?.isIndeterminate = true

        dialog?.show()
        dialog?.window?.setGravity(Gravity.CENTER)

    }
    return dialog
}

fun Context?.hideLoader() {
    if (dialog?.isShowing == true) {
        dialog?.dismiss()
    }
}

fun getCurrentDateTime(): String? {
    val outputDateFormat = SimpleDateFormat(Constants.systemDateTimeFormat, Locale.getDefault())
    return outputDateFormat.format(Calendar.getInstance().time.time)
}

fun getCurrentDateTimeForServer(): String? {
    val outputDateFormat = SimpleDateFormat(Constants.serverApiDateTimeFormat, Locale.getDefault())
    return outputDateFormat.format(Calendar.getInstance().time.time)
}



fun getLocationComponentsFromAddress(context: Context, addressString: String): String {
    val geocoder = Geocoder(context, Locale.getDefault())
    val addressList: MutableList<Address>? = geocoder.getFromLocationName(addressString, 1)

    return if (addressList.isNullOrEmpty().not()) {
        val address = addressList?.get(0)

        address?.locality ?: address?.subAdminArea ?: address?.adminArea ?: address?.countryName.toString()

       } else {
        addressString.split(",").last()
    }
}

 fun Context?.getAddress(latLng: LatLng): String {
    val geocoder = this?.let { Geocoder(it, Locale.getDefault()) }
    val address: Address?
    var addressText = ""

    val addresses: List<Address>? =
        geocoder?.getFromLocation(latLng.latitude, latLng.longitude, 2)

    if (addresses != null) {
        if (addresses.isNotEmpty()) {
            address = addresses[0]
            addressText = address.getAddressLine(0)
        } else {
            addressText = "its not appear"
        }
    }
    return addressText
}

fun calculateDistance(primaryLat: Double, primaryLng: Double, targetLat: Double, targetLng: Double): Double {
    val earthRadius = 6371.0 // Radius of the Earth in kilometers

    val dLat = Math.toRadians(targetLat - primaryLat)
    val dLng = Math.toRadians(targetLng - primaryLng)

    val a = sin(dLat / 2).pow(2.0) + cos(Math.toRadians(primaryLat)) * cos(Math.toRadians(targetLat)) * sin(dLng / 2).pow(2.0)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return earthRadius * c
}
