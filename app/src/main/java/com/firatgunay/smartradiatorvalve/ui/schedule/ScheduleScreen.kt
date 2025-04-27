package com.firatgunay.smartradiatorvalve.ui.schedule

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.firatgunay.smartradiatorvalve.data.model.DayOfWeek
import com.firatgunay.smartradiatorvalve.data.model.Schedule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val schedules by viewModel.schedules.collectAsState()
    val selectedDay by viewModel.selectedDay.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Program") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Gün seçici
            DaySelector(
                selectedDay = selectedDay,
                onDaySelected = { day ->
                    viewModel.setSelectedDay(day.value)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Hata mesajı
            error?.let { errorMessage ->
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(8.dp)
                )
            }

            // Yükleniyor göstergesi veya program listesi
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                if (schedules.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Bu güne ait program bulunmuyor",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(schedules) { schedule ->
                            ScheduleItem(
                                schedule = schedule,
                                onDelete = { viewModel.deleteSchedule(schedule) },
                                onEdit = { viewModel.editSchedule(schedule) }
                            )
                        }
                    }
                }
            }

            // Program ekle butonu
            FloatingActionButton(
                onClick = { viewModel.createNewSchedule(selectedDay) },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Program Ekle")
            }

            if (showAddDialog) {
                AddScheduleDialog(
                    selectedDay = selectedDay,
                    onDismiss = { showAddDialog = false },
                    onConfirm = { schedule ->
                        viewModel.addSchedule(schedule)
                        showAddDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun DaySelector(
    selectedDay: Int,
    onDaySelected: (DayOfWeek) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(DayOfWeek.values()) { day ->
            DayChip(
                day = day,
                isSelected = selectedDay == day.value,
                onClick = { onDaySelected(day) }
            )
        }
    }
}

@Composable
fun DayChip(
    day: DayOfWeek,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = day.turkishName,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun ScheduleItem(
    schedule: Schedule,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${schedule.startTime} - ${schedule.endTime}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "${schedule.targetTemperature}°C",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Sil")
            }
        }
    }
}

@Composable
fun AddScheduleDialog(
    selectedDay: Int,
    onDismiss: () -> Unit,
    onConfirm: (Schedule) -> Unit
) {
    var startTime by remember { mutableStateOf("00:00") }
    var endTime by remember { mutableStateOf("23:59") }
    var temperature by remember { mutableStateOf("21.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Program Ekle") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    label = { Text("Başlangıç Saati (HH:mm)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = endTime,
                    onValueChange = { endTime = it },
                    label = { Text("Bitiş Saati (HH:mm)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = temperature,
                    onValueChange = { temperature = it },
                    label = { Text("Hedef Sıcaklık (°C)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        Schedule(
                            dayOfWeek = selectedDay,
                            startTime = startTime,
                            endTime = endTime,
                            targetTemperature = temperature.toFloatOrNull() ?: 21.0f
                        )
                    )
                }
            ) {
                Text("Ekle")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
} 