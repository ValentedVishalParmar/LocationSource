package com.libertyinfosace.locationsource.ui.adapter

import android.annotation.SuppressLint
import android.icu.math.BigDecimal
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.libertyinfosace.locationsource.R
import com.libertyinfosace.locationsource.data.local.model.LocationDataEntity
import com.libertyinfosace.locationsource.databinding.ItemLocationListBinding
import com.libertyinfosace.locationsource.interfaces.OnItemClickCListener
import com.libertyinfosace.locationsource.util.setText
import com.libertyinfosace.locationsource.util.viewGone
import com.libertyinfosace.locationsource.util.viewVisible


class LocationAdapter(private var context: AppCompatActivity?, private var arrayListLocation: ArrayList<LocationDataEntity>, private val onItemClickCListener: OnItemClickCListener) : RecyclerView.Adapter<LocationAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_location_list, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position in arrayListLocation.indices) {
            val productData = arrayListLocation[position]
            holder.bind(productData)
        }
    }

    override fun getItemCount(): Int {
        return arrayListLocation.size
    }

    inner class ViewHolder(private val binding: ItemLocationListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(locationDatas: LocationDataEntity?) {
            locationDatas?.let { locationData ->
                with(binding) {

                    binding.locationData = locationData
                    executePendingBindings()

                    // place name
                    setText(tvName, locationData?.name ?: "-")

                    //place is primary
                    if (locationData.isPrimary == true) {
                        viewVisible(btnPrimaryLocation)

                    } else {
                        viewGone(btnPrimaryLocation)
                    }

                    // distance in  KM

                    if (locationData.isPrimary == false) {
                        locationData.distance?.let { distanceInKm ->
                            setText(tvDistance, context?.getString(R.string.distance_f_km, distanceInKm))
                            viewVisible(tvDistance)
                        }

                    } else {
                        viewGone(tvDistance)
                    }

                    // place address
                    setText(tvAddress, locationData.address ?: "-")

                    ivEdit.setOnClickListener {
                        onItemClickCListener.onUpdateLocation(bindingAdapterPosition)
                    }

                    ivDelete.setOnClickListener {
                        onItemClickCListener.onDeleteLocation(bindingAdapterPosition)
                    }
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateNewList(newList: ArrayList<LocationDataEntity>?) {
        newList?.let { list ->
            this@LocationAdapter.arrayListLocation = list
            notifyDataSetChanged()
        }
    }

    fun appendProducts(newProducts: List<LocationDataEntity>?) {
        val startPosition = arrayListLocation.size
        if (newProducts != null) {
            arrayListLocation.clear()
            arrayListLocation.addAll(newProducts)
            notifyItemRangeInserted(startPosition, newProducts.size)
        }
    }
}
