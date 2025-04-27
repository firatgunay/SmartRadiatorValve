package com.firatgunay.smartradiatorvalve.ui.schedule

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.firatgunay.smartradiatorvalve.data.model.Schedule
import com.firatgunay.smartradiatorvalve.data.model.DayOfWeek
import com.firatgunay.smartradiatorvalve.data.repository.ValveRepository
import com.firatgunay.smartradiatorvalve.data.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.Calendar

private const val TAG = "ScheduleViewModel"

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val valveRepository: ValveRepository,
    private val scheduleRepository: ScheduleRepository
) : ViewModel() {

    private val _schedules = MutableStateFlow<List<Schedule>>(emptyList())
    val schedules: StateFlow<List<Schedule>> = _schedules.asStateFlow()

    private val _selectedDay = MutableStateFlow(getCurrentDayOfWeek())
    val selectedDay: StateFlow<Int> = _selectedDay.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadSchedules()
    }

    fun setSelectedDay(day: Int) {
        _selectedDay.value = day
        loadSchedules()
    }

    private fun loadSchedules() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                scheduleRepository.getSchedulesForDay(_selectedDay.value)
                    .collect { scheduleList ->
                        _schedules.value = scheduleList
                        _error.value = null
                    }
            } catch (e: Exception) {
                _error.value = "Programlar yüklenirken hata oluştu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addSchedule(schedule: Schedule) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                scheduleRepository.insertSchedule(schedule)
                loadSchedules()
            } catch (e: Exception) {
                _error.value = "Program eklenirken bir hata oluştu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteSchedule(schedule: Schedule) {
        viewModelScope.launch {
            try {
                scheduleRepository.deleteSchedule(schedule)
                loadSchedules()
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Program silinirken hata oluştu: ${e.message}"
            }
        }
    }

    fun editSchedule(schedule: Schedule) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                Log.d(TAG, "Program güncelleniyor: $schedule")
                
                scheduleRepository.updateSchedule(schedule)
                
                Log.d(TAG, "Program başarıyla güncellendi")
                loadSchedules()
            } catch (e: Exception) {
                Log.e(TAG, "Program güncellenirken hata oluştu", e)
                _error.value = "Program güncellenirken bir hata oluştu: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createNewSchedule(dayOfWeek: Int) {
        viewModelScope.launch {
            val newSchedule = Schedule.createDefault(dayOfWeek)
            scheduleRepository.insertSchedule(newSchedule)
            loadSchedules()
        }
    }

    private fun getCurrentDayOfWeek(): Int {
        return Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            valveRepository.cleanup()
        }
    }
} 