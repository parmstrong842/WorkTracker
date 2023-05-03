package com.example.worktracker.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.worktracker.AppViewModelProvider
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navigateBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Settings") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        content = {
            Column(
                modifier = Modifier.padding(
                    top = it.calculateTopPadding() + 16.dp,
                    bottom = it.calculateBottomPadding() + 16.dp,
                    start = it.calculateStartPadding(LocalLayoutDirection.current) + 16.dp,
                    end = it.calculateEndPadding(LocalLayoutDirection.current) + 16.dp
                )
            ) {
                TimeZoneSetting(
                    onTimeZoneSelected = { timeZone ->
                        viewModel.updateTimeZone(timeZone)
                    }
                )
                StartOfWeekSetting(
                    onStartOfWeekSelected = { startOfWeek ->
                        viewModel.updateStartOfWeek(startOfWeek)
                    }
                )
            }
        }
    )
}

@Composable
fun TimeZoneSetting(
    onTimeZoneSelected: (String) -> Unit
) {
    val showDialog = remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "Time Zone", modifier = Modifier.weight(1f))
        Button(onClick = { showDialog.value = true }) {
            Text(text = "Select")
        }
    }
    Divider()

    TimeZoneSelectionDialog(
        showDialog = showDialog.value,
        onDismissRequest = { showDialog.value = false },
        onTimeZoneSelected = onTimeZoneSelected
    )
}

@Composable
fun TimeZoneSelectionDialog(
    showDialog: Boolean,
    onDismissRequest: () -> Unit,
    onTimeZoneSelected: (String) -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = { Text(text = "Select Time Zone") },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    val timeZones = TimeZone.getAvailableIDs().toList()
                    items(timeZones) { timeZone ->
                        Text(
                            text = timeZone,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable {
                                    onTimeZoneSelected(timeZone)
                                    onDismissRequest()
                                }
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }
}

@Composable
fun StartOfWeekSetting(onStartOfWeekSelected: (String) -> Unit) {
    val showDialog = remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "Start of Week", modifier = Modifier.weight(1f))
        Button(onClick = { showDialog.value = true }) {
            Text(text = "Select")
        }
    }
    Divider()

    WeekSelectionDialog(
        showDialog = showDialog.value,
        onDismissRequest = { showDialog.value = false },
        onStartOfWeekSelected = onStartOfWeekSelected
    )
}

@Composable
fun WeekSelectionDialog(
    showDialog: Boolean,
    onDismissRequest: () -> Unit,
    onStartOfWeekSelected: (String) -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = { Text(text = "Select Start of Week") },
            text = {
                Column {
                    val weekStartOptions = listOf("Saturday", "Sunday", "Monday")
                    weekStartOptions.forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier
                                .padding(8.dp)
                                .clickable {
                                    onStartOfWeekSelected(day.uppercase())
                                    onDismissRequest()
                                }
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }
}