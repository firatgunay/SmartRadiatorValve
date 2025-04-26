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
    entities = [Schedule::class, ValveData::class],
    version = 2,
    exportSchema = false // Schema export'u devre dışı bırak
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scheduleDao(): ScheduleDao
    abstract fun valveDataDao(): ValveDataDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
} 