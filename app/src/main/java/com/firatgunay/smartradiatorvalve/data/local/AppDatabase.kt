package com.firatgunay.smartradiatorvalve.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.firatgunay.smartradiatorvalve.data.local.dao.ScheduleDao
import com.firatgunay.smartradiatorvalve.data.local.dao.ValveDataDao
import com.firatgunay.smartradiatorvalve.data.model.Schedule
import com.firatgunay.smartradiatorvalve.data.model.ValveData

@Database(
    entities = [
        ValveData::class,
        Schedule::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun valveDataDao(): ValveDataDao
    abstract fun scheduleDao(): ScheduleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_radiator_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 