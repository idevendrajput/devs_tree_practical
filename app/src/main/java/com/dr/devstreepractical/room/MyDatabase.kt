package com.dr.devstreepractical.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [LocationsEntity::class], version = 2)
abstract class MyDatabase : RoomDatabase() {

    abstract fun locationDao() : LocationsDao

    companion object {
        private var INSTANCE: MyDatabase? = null
        fun getDatabase(context: Context): MyDatabase {
            if (INSTANCE == null) {
                synchronized(this) {
                    INSTANCE =
                        Room.databaseBuilder(context, MyDatabase::class.java, "my_database")
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries()
                            .build()
                }
            }
            return INSTANCE!!
        }
    }

}