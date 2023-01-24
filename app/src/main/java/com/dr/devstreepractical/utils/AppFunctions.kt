package com.dr.devstreepractical.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Index
import com.dr.devstreepractical.room.LocationsEntity
import com.dr.devstreepractical.room.MyDatabase
import com.dr.devstreepractical.utils.AppConst.MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
import com.google.android.gms.maps.model.LatLng
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

object AppFunctions {


    fun getLatLang(placeId: String): LatLng? {

        try {

            val baseUrl =
                "https://maps.googleapis.com/maps/api/place/details/json?placeid=$placeId&key=${AppConst.MAP_API_KEY}"

            OkHttpClient().newCall(
                Request.Builder().url(baseUrl).build()
            ).execute().also {
                val resultObj = it.body?.string()
                    ?.let { it1 -> JSONObject(it1).getJSONObject("result") }
                val geometryObj = resultObj?.getJSONObject("geometry")
                val locationObj = geometryObj?.getJSONObject("location")
                val latitude = locationObj?.get("lat").toString().toDouble()
                val longitude = locationObj?.get("lng").toString().toDouble()
                Log.d("TAG", "getLatLang: $it")
                return LatLng(latitude, longitude)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

    }

     fun getDistance(latLng: LatLng?, activity: AppCompatActivity): Double {

        val destLocation = Location("")

        destLocation.latitude = latLng?.latitude ?: .0
        destLocation.longitude = latLng?.longitude ?: .0

        val distance = getMyLocation(activity).distanceTo(destLocation) //in meters

        return distance.toDouble() / 1000
    }


    private fun getMyLocation(activity: AppCompatActivity): Location  {

        val myLatLng = getMyLatLng(activity)
        val myLocation = Location("")
        myLocation.latitude = myLatLng.latitude
        myLocation.longitude = myLatLng.longitude

        return myLocation
    }

    fun getMyLatLng(activity: AppCompatActivity): LatLng {
        val locationManager =
            activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val locationProvider = LocationManager.NETWORK_PROVIDER

        @SuppressLint("MissingPermission")
        val lastKnownLocation = locationManager.getLastKnownLocation(locationProvider)
        val userLat = lastKnownLocation?.latitude
        val userLong = lastKnownLocation?.longitude

        return LatLng(userLat ?: .0, userLong ?: .0)
    }

    fun checkLocationPermissions(activity: AppCompatActivity, isGranted: (isGranted: Boolean) -> Unit) {

                if (ContextCompat.checkSelfPermission(activity,android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                    isGranted(false)
                    ActivityCompat.requestPermissions(activity,
                        arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)

                } else {
                   isGranted(true)
                }
    }

    fun getLocations(room: MyDatabase?, order: Index.Order): List<LocationsEntity> {
       return  (if (order == Index.Order.ASC) room?.locationDao()?.getLocationsAsc() else room?.locationDao()?.getLocationsDesc()) ?: emptyList()
    }


}