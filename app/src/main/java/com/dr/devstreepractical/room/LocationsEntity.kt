package com.dr.devstreepractical.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dr.devstreepractical.utils.AppConst

@Entity(tableName = AppConst.LOCATION_TABLE)
data class LocationsEntity (
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null,
    var cityName: String? = null,
    var address: String? = null,
    var placeId: String? = null,
    var lat: Double = .0,
    var long: Double = .0,
    var distance: Double = .0
)