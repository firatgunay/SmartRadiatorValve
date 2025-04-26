package com.firatgunay.smartradiatorvalve.data.local.dao

import androidx.room.*
import com.firatgunay.smartradiatorvalve.data.model.ValveData
import kotlinx.coroutines.flow.Flow

@Dao
interface ValveDataDao {
    @Query("SELECT * FROM valve_data ORDER BY timestamp DESC LIMIT 1")
    fun getLatestData(): Flow<ValveData?>
    
    @Query("SELECT * FROM valve_data ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentData(limit: Int): Flow<List<ValveData>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertData(data: ValveData)
    
    @Query("DELETE FROM valve_data WHERE timestamp < :timestamp")
    suspend fun deleteOldData(timestamp: Long)
} 