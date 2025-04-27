package com.firatgunay.smartradiatorvalve.data.repository

import com.firatgunay.smartradiatorvalve.data.local.dao.ScheduleDao
import com.firatgunay.smartradiatorvalve.data.model.Schedule
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleRepository @Inject constructor(
    private val scheduleDao: ScheduleDao
) {
    fun getSchedulesForDay(dayOfWeek: Int): Flow<List<Schedule>> {
        return scheduleDao.getSchedulesForDay(dayOfWeek)
    }

    suspend fun getAllSchedules(): List<Schedule> {
        return scheduleDao.getAllSchedules()
    }

    suspend fun insertSchedule(schedule: Schedule) {
        scheduleDao.insertSchedule(schedule)
    }

    suspend fun updateSchedule(schedule: Schedule) {
        scheduleDao.updateSchedule(schedule)
    }

    suspend fun deleteSchedule(schedule: Schedule) {
        scheduleDao.deleteSchedule(schedule)
    }
} 