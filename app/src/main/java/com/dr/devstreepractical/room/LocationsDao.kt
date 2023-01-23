package com.dr.devstreepractical.room

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface LocationsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: LocationsEntity)

    @Update
    suspend fun update(entity: LocationsEntity)

    @Query("DELETE FROM locations WHERE id == :id")
    fun deleteById(id: Long)

    @Query("SELECT * FROM locations WHERE id == :id LIMIT 1")
    fun getLocationById(id: Long): LocationsEntity?

    @Query("SELECT * FROM locations ORDER BY distance ASC")
    fun getLocationsAsc(): List<LocationsEntity>

    @Query("SELECT * FROM locations ORDER BY distance DESC")
    fun getLocationsDesc(): List<LocationsEntity>

}