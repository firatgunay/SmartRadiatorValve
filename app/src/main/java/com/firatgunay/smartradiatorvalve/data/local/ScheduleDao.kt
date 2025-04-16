import androidx.room.*
import com.firatgunay.smartradiatorvalve.navigation.NavRoute
import kotlinx.coroutines.flow.Flow
import com.firatgunay.smartradiatorvalve.data.model.Schedule

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedules WHERE dayOfWeek = :dayOfWeek ORDER BY startTime")
    suspend fun getSchedulesForDay(dayOfWeek: Int): List<Schedule>

    @Query("SELECT * FROM schedules ORDER BY dayOfWeek, startTime")
    suspend fun getAllSchedules(): List<Schedule>

    @Query("SELECT * FROM schedules WHERE id = :id")
    suspend fun getScheduleById(id: Long): Schedule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: Schedule): Long

    @Update
    suspend fun updateSchedule(schedule: Schedule)

    @Delete
    suspend fun deleteSchedule(schedule: Schedule)

    @Query("DELETE FROM schedules WHERE id = :scheduleId")
    suspend fun deleteScheduleById(scheduleId: Long)

    @Query("DELETE FROM schedules WHERE dayOfWeek = :dayOfWeek")
    suspend fun deleteSchedulesForDay(dayOfWeek: Int)

    @Query("SELECT COUNT(*) FROM schedules WHERE dayOfWeek = :dayOfWeek")
    suspend fun getScheduleCountForDay(dayOfWeek: Int): Int

    @Query("SELECT * FROM schedules WHERE dayOfWeek = :dayOfWeek AND startTime <= :currentTime AND endTime >= :currentTime LIMIT 1")
    suspend fun getCurrentActiveSchedule(dayOfWeek: Int, currentTime: String): Schedule?

    @Transaction
    suspend fun replaceSchedulesForDay(dayOfWeek: Int, newSchedules: List<Schedule>) {
        deleteSchedulesForDay(dayOfWeek)
        newSchedules.forEach { schedule ->
            insertSchedule(schedule.copy(dayOfWeek = dayOfWeek))
        }
    }
} 