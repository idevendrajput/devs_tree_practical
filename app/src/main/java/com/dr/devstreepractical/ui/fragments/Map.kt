package com.dr.devstreepractical.ui.fragments

import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.dr.devstreepractical.R
import com.dr.devstreepractical.base.BaseFragment
import com.dr.devstreepractical.databinding.FragmentMapBinding
import com.dr.devstreepractical.room.LocationsEntity
import com.dr.devstreepractical.ui.dialogs.find_places.FindPlaces
import com.dr.devstreepractical.utils.AppConst
import com.dr.devstreepractical.utils.AppConst.IS_ROUTE_MODE
import com.dr.devstreepractical.utils.AppConst.MAP_API_KEY
import com.dr.devstreepractical.utils.AppFunctions
import com.dr.devstreepractical.utils.AppFunctions.getDistance
import com.dr.devstreepractical.utils.AppFunctions.getLatLang
import com.dr.devstreepractical.utils.AppFunctions.getMyLatLng
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class Map : BaseFragment<FragmentMapBinding>(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    GoogleMap.OnMarkerDragListener,
    GoogleMap.OnInfoWindowClickListener {

    private val job = CoroutineScope(IO)

    private var googleMap: GoogleMap? = null
    private var location: LocationsEntity? = null

    private var locationId: Long? = null
    private var isEdit: Boolean = false
    private var isRouteMode: Boolean = false

    override fun init() {

        val supportMapFragment =
            childFragmentManager.findFragmentByTag("mapFragment") as SupportMapFragment?
        supportMapFragment?.getMapAsync(this)

        isRouteMode = arguments?.getBoolean(IS_ROUTE_MODE, false) ?: false

        ifEditMode()

    }

    private fun ifEditMode() {

        locationId = arguments?.getLong(AppConst.LOCATION_ID)

        Log.d(TAG, "ifEditMode: $locationId")

        if (locationId != null) {
            location = room?.locationDao()?.getLocationById(locationId!!)
            if (location != null && location?.id != null) {
                isEdit = true
                Log.d(TAG, "ifEditMode: ${location?.distance}")
            }
        }

    }

    override fun getViewBinding() = FragmentMapBinding.inflate(layoutInflater)

    override fun actions() {
        binding.btnSearch.setOnClickListener {
            findPlaces()
        }
        binding.tb.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.save.setOnClickListener {
            if (location != null) {
                saveLocation()
            }
        }
    }

    private fun findPlaces() {
        FindPlaces(mContext) {
            location = if (location == null) LocationsEntity() else location
            showLoading()
            job.launch {
                val latLng = it.placeId?.let { it1 -> getLatLang(it1) }
                location.apply {
                    this?.distance = it.distance
                    this?.address = it.address
                    this?.cityName = it.cityName
                    this?.placeId = it.placeId
                    this?.lat = latLng?.latitude ?: .0
                    this?.long = latLng?.longitude ?: .0
                    this?.distance = getDistance(latLng, activity as AppCompatActivity)
                }
                Log.d(TAG, "findPlaces: ${latLng?.latitude} ${latLng?.longitude}")
                withContext(Main) {
                    dismissLoading()
                    focusOnPlace()
                    binding.saveAlertDialog.isVisible = true
                }
            }
        }.build().show()
    }

    private fun focusOnPlace() {

        with(googleMap) {
            this?.mapType = GoogleMap.MAP_TYPE_TERRAIN
            this?.setInfoWindowAdapter(CustomInfoWindowAdapter())
            this?.setOnMarkerClickListener(this@Map)
            this?.setOnInfoWindowClickListener(this@Map)
            this?.setOnMarkerDragListener(this@Map)
            this?.setContentDescription("Map with marker.")
        }

        if (location != null) {
            addMarker(location!!)
        }

    }

    private fun addMarker(location: LocationsEntity) {
        googleMap?.addMarker(
            MarkerOptions()
                .position(LatLng(location.lat, location.long))
                .title(location.cityName)
                .icon(
                    BitmapDescriptorFactory.fromResource(R.drawable.pin)
                )
                .snippet(location.address)
                .rotation(-25f)
        )?.showInfoWindow()
        googleMap?.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(location.lat, location.long),
                if (isRouteMode) 10f else 16f
            )
        )
    }

    private fun addMarkerAtMyLocation() {
        googleMap?.addMarker(
            MarkerOptions()
                .position(getMyLatLng(activity as AppCompatActivity))
                .icon(
                    BitmapDescriptorFactory.fromResource(R.drawable.pin)
                )
                .rotation(-25f)
        )
    }

    private fun saveLocation() {
        Log.d(TAG, "saveLocation: $isEdit")
        job.launch {
            location?.let { room?.locationDao()?.insert(it) }
            Log.d(TAG, "saveLocation: ${location?.id}")
            withContext(Main) {
                findNavController().popBackStack()
            }
        }
    }

    internal inner class CustomInfoWindowAdapter : GoogleMap.InfoWindowAdapter {

        private val window: View = layoutInflater.inflate(android.R.layout.simple_list_item_1, null)

        override fun getInfoWindow(marker: Marker): View {
            render(marker, window)
            return window
        }

        override fun getInfoContents(marker: Marker): View? {
            return null
        }

        private fun render(marker: Marker, view: View) {
            view.background = ContextCompat.getDrawable(mContext, R.drawable.rounded_bg_10)
            val title: String? = marker.title
            val titleUi = view.findViewById<TextView>(android.R.id.text1)

            titleUi.text = title ?: ""

        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {

        this.googleMap = googleMap
        val indiaBounds = LatLngBounds(
            LatLng(8.17, 68.75), LatLng(37.09, 97.40)
        )

        googleMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(indiaBounds, 0))

        if (isEdit) {
            focusOnPlace()
        }

        Log.d(TAG, "onMapReady: $isRouteMode")
        if (isRouteMode) {
            createMapRoutes()
        }

    }

    private fun createMapRoutes() {

        val locations = room?.locationDao()?.getLocationsAsc()

        addMarkerAtMyLocation()

        if (locations != null && locations.isNotEmpty()) {
            val locationsLatLng = ArrayList<LatLng>()
            locations.mapIndexed { index, locationsEntity ->
                locationsLatLng.add(LatLng(locationsEntity.lat, locationsEntity.long))
                addMarker(locationsEntity)
                showLoading("Getting directions")
                getDirections(
                    if (index == 0) getMyLatLng(activity as AppCompatActivity) else LatLng(
                        locations[index - 1].lat,
                        locations[index - 1].long
                    ), LatLng(locationsEntity.lat, locationsEntity.long)
                )
            }
        }
    }

    private fun getDirections(origin: LatLng, destination: LatLng) {
        job.launch {
            val url =
                "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${destination.latitude},${destination.longitude}&key=$MAP_API_KEY"
            val result = withContext(IO) {
                val myUrl = URL(url)
                val httpURLConnection = myUrl.openConnection() as HttpURLConnection
                httpURLConnection.connect()
                val istm = httpURLConnection.inputStream
                val bufferedReader = BufferedReader(InputStreamReader(istm))
                val stringBuilder = StringBuilder()
                var line: String? = ""
                while (line != null) {
                    line = bufferedReader.readLine()
                    stringBuilder.append(line)
                }
                val data = stringBuilder.toString()
                val jsonObject = JSONObject(data)
                val jsonObject1 = jsonObject.getJSONArray("routes").getJSONObject(0)
                val result = jsonObject1.getJSONObject("overview_polyline").getString("points")
                result
            }
            withContext(Main) {
                val list = PolyUtil.decode(result)
                googleMap?.addPolyline(PolylineOptions().addAll(list).color(Color.RED).width(10f))
                dismissLoading()
            }
        }
    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        Log.d(TAG, "onMarkerClick: ")
        return false
    }

    override fun onMarkerDragStart(p0: Marker?) {
        Log.d(TAG, "onMarkerDragStart: ")
    }

    override fun onMarkerDrag(p0: Marker?) {
        Log.d(TAG, "onMarkerDrag: ")
    }

    override fun onMarkerDragEnd(p0: Marker?) {
        Log.d(TAG, "onMarkerDragEnd: ")
    }

    override fun onInfoWindowClick(p0: Marker?) {
        Log.d(TAG, "onInfoWindowClick: ")
    }

}