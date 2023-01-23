package com.dr.devstreepractical.ui.dialogs.find_places

import android.content.Context
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.dr.devstreepractical.databinding.DialogFindPlacesBinding
import com.dr.devstreepractical.room.LocationsEntity
import com.dr.devstreepractical.ui.adapters.AdapterSearches
import com.dr.devstreepractical.utils.AppConst
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import java.util.ArrayList

class FindPlaces(private val mContext: Context, val callBack: (place: LocationsEntity) -> Unit) : BottomSheetDialog(mContext) {

    private val binding = DialogFindPlacesBinding.inflate(layoutInflater)

    private val placesClient by lazy {
        Places.createClient(mContext)
    }

    fun build() = run {

        setContentView(binding.root)
        window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        actions()
        this
    }

    private fun actions() {

        binding.etSearch.addTextChangedListener {
            binding.pb.isVisible = true
            CoroutineScope(IO).launch {
                findPlaces(it.toString())
            }
        }

    }

    private fun findPlaces(query: String) {

        Places.initialize(mContext, AppConst.MAP_API_KEY)

        val token = AutocompleteSessionToken.newInstance()

        val bounds = RectangularBounds.newInstance(
            LatLng(-33.880490, 151.184363),  //dummy lat/lng
            LatLng(-33.880490, 151.184363)
        )

        val request = FindAutocompletePredictionsRequest.builder()
            .setLocationBias(bounds)
            .setCountry("in")
            .setTypeFilter(TypeFilter.ADDRESS)
            .setSessionToken(token)
            .setQuery(query)
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->

                val list = ArrayList<LocationsEntity>()

                for (prediction in response.autocompletePredictions) {
                    list.add(
                        LocationsEntity(
                            cityName = prediction.getPrimaryText(null).toString(),
                            address = prediction.getSecondaryText(null).toString(),
                            placeId = prediction.placeId, lat = .0, long = .0, distance = .0
                        )
                    )
                }

                CoroutineScope(Main).launch {
                    val adapterNotifications = AdapterSearches(list) {
                        this@FindPlaces.dismiss()
                        callBack(it)
                    }

                    binding.rv.adapter = adapterNotifications
                    binding.pb.isVisible = false
                }

            }.addOnFailureListener { e: Exception? ->
                e?.printStackTrace()
            }

    }



}