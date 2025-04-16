package com.firatgunay.smartradiatorvalve.ui.schedule

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.firatgunay.smartradiatorvalve.data.local.dao.ScheduleDao
import com.firatgunay.smartradiatorvalve.data.model.Schedule
import com.firatgunay.smartradiatorvalve.data.repository.ValveRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ScheduleViewModel"

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val valveRepository: ValveRepository
) : ViewModel() {

    private val _schedules = MutableStateFlow<List<Schedule>>(emptyList())
    val schedules: StateFlow<List<Schedule>> = _schedules.asStateFlow()

    private val _selectedDay = MutableStateFlow(1) // Pazartesi
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
                valveRepository.getSchedulesForDay(_selectedDay.value)
                    .collect { schedules ->
                        _schedules.value = schedules
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
                valveRepository.insertSchedule(schedule)
                loadSchedules()
            } catch (e: Exception) {
                _error.value = "Program eklenirken bir hata oluştu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteSchedule(id: Long) {
        viewModelScope.launch {
            try {
                valveRepository.deleteSchedule(id)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Program silinirken hata oluştu: ${e.message}"
            }
        }
    }

    fun updateSchedule(schedule: Schedule) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                Log.d(TAG, "Program güncelleniyor: $schedule")
                
                valveRepository.updateSchedule(schedule)
                
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

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            valveRepository.cleanup()
        }
    }
} 