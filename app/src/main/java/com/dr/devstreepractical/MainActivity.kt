package com.dr.devstreepractical

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.database.Observable
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.dr.devstreepractical.base.BaseActivity
import com.dr.devstreepractical.databinding.ActivityMainBinding
import com.dr.devstreepractical.utils.AppConst
import com.dr.devstreepractical.utils.AppFunctions
import java.util.concurrent.Executors

class MainActivity : BaseActivity<ActivityMainBinding>() {

    companion object {

        val isLocationPermissionGranted = MutableLiveData<Boolean>()

    }

    override fun getViewBinding() = ActivityMainBinding.inflate(layoutInflater)

    override fun init() {

        AppFunctions.checkLocationPermissions(this) {
            isLocationPermissionGranted.postValue(it)
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == AppConst.MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            isLocationPermissionGranted.postValue(true)
        } else {
            isLocationPermissionGranted.postValue(false)
        }

    }

}