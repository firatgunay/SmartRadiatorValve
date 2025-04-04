package com.firatgunay.smartradiatorvalve.ui.schedule

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.firatgunay.smartradiatorvalve.data.model.Schedule
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private const val TAG = "ScheduleViewModel"

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _schedules = MutableStateFlow<List<Schedule>>(emptyList())
    val schedules: StateFlow<List<Schedule>> = _schedules.asStateFlow()

    private val _selectedDay = MutableStateFlow<Int>(1) // Pazartesi
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
            try {
                _isLoading.value = true
                _error.value = null
                Log.d(TAG, "Programlar yükleniyor... Seçili gün: ${_selectedDay.value}")
                
                val schedulesList = firestore.collection("schedules")
                    .whereEqualTo("dayOfWeek", _selectedDay.value)
                    .get()
                    .await()
                    .documents
                    .mapNotNull { doc ->
                        doc.toObject(Schedule::class.java)?.copy(id = doc.id)
                    }
                
                Log.d(TAG, "Yüklenen program sayısı: ${schedulesList.size}")
                _schedules.value = schedulesList
            } catch (e: Exception) {
                Log.e(TAG, "Program yüklenirken hata oluştu", e)
                _error.value = "Programlar yüklenirken bir hata oluştu: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addSchedule(schedule: Schedule) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                Log.d(TAG, "Yeni program ekleniyor: $schedule")
                
                val docRef = firestore.collection("schedules").document()
                val scheduleWithId = schedule.copy(id = docRef.id)
                docRef.set(scheduleWithId).await()
                
                Log.d(TAG, "Program başarıyla eklendi")
                loadSchedules()
            } catch (e: Exception) {
                Log.e(TAG, "Program eklenirken hata oluştu", e)
                _error.value = "Program eklenirken bir hata oluştu: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteSchedule(scheduleId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                Log.d(TAG, "Program siliniyor: $scheduleId")
                
                firestore.collection("schedules")
                    .document(scheduleId)
                    .delete()
                    .await()
                
                Log.d(TAG, "Program başarıyla silindi")
                loadSchedules()
            } catch (e: Exception) {
                Log.e(TAG, "Program silinirken hata oluştu", e)
                _error.value = "Program silinirken bir hata oluştu: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSchedule(schedule: Schedule) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                Log.d(TAG, "Program güncelleniyor: $schedule")
                
                firestore.collection("schedules")
                    .document(schedule.id)
                    .set(schedule)
                    .await()
                
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
} 