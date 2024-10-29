package com.example.worktracker.ui

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.worktracker.AppViewModelProvider
import com.example.worktracker.R
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object ShiftEditDestination {
    const val route = "shift_edit"
    const val shiftIdArg = "shiftId"
    const val routeWithArgs = "$route/{$shiftIdArg}"
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ShiftEditScreen(
    topBarTitle: String,
    navigateBack: () -> Unit,
    viewModel: ShiftEditViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var openBreakDialog by rememberSaveable { mutableStateOf(false) }
    var breakText by rememberSaveable { mutableStateOf("") }

    var openDeleteDialog by rememberSaveable { mutableStateOf(false) }

    val weight = 1.5f

    Scaffold(
        topBar = {
            ShiftTopAppBar(
                title = topBarTitle,
                navigateUp = navigateBack
            )
        }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 15.dp)
        ) {
            Spacer(Modifier.padding(80.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                Text(
                    text = stringResource(R.string.shift_start),
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = {
                        val (cYear, cMonth, cDayOfMonth) = viewModel.getDatePickerStart()
                        val listener = OnDateSetListener { _, year, month, dayOfMonth ->
                            viewModel.updateStartDate(year, month, dayOfMonth)
                            viewModel.updateEndDate(year, month, dayOfMonth)
                            viewModel.updateTotal()
                        }
                        DatePickerDialog(context, listener, cYear, cMonth, cDayOfMonth).show()
                    },
                    modifier = Modifier.weight(weight)
                ) {
                    Text(
                        text = uiState.startDate,
                        fontSize = 15.sp,
                        modifier = Modifier.testTag("startDate")
                    )
                }
                TextButton(
                    onClick = {
                        val time = getHourAndMinute(uiState.startTime)
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                viewModel.updateStartTime(hour, minute)
                                viewModel.updateTotal()
                            },
                            time.first, time.second, false
                        ).show()
                    },
                    modifier = Modifier.weight(weight)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = uiState.startTime,
                            fontSize = 15.sp,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("startTime"),
                            textAlign = TextAlign.Center

                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = stringResource(R.string.select_start_time)
                        )
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                Text(
                    text = stringResource(R.string.shift_end),
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = {
                        val (cYear, cMonth, cDayOfMonth) = viewModel.getDatePickerEnd()
                        val listener = OnDateSetListener { _, year, month, dayOfMonth ->
                            viewModel.updateEndDate(year, month, dayOfMonth)
                            viewModel.updateTotal()
                        }
                        DatePickerDialog(context, listener, cYear, cMonth, cDayOfMonth).show()
                    },
                    modifier = Modifier.weight(weight)
                ) {
                    Text(
                        text = uiState.endDate,
                        fontSize = 15.sp,
                        modifier = Modifier.testTag("endDate")
                    )
                }
                TextButton(
                    onClick = {
                        val time = getHourAndMinute(uiState.endTime)
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                viewModel.updateEndTime(hour, minute)
                                viewModel.updateTotal()
                            },
                            time.first, time.second, false
                        ).show()
                    },
                    modifier = Modifier.weight(weight)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = uiState.endTime,
                            fontSize = 15.sp,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("endTime"),
                            textAlign = TextAlign.Center
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = stringResource(R.string.select_end_time)
                        )
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                Text(
                    text = stringResource(R.string.break_item),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = uiState.breakTotal,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(weight)
                )
                TextButton(
                    onClick = {
                        openBreakDialog = true
                        breakText = ""
                    },
                    modifier = Modifier.weight(weight)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween//********
                    ) {
                        Text(
                            text = "",
                            fontSize = 15.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = stringResource(R.string.select_break_time)
                        )
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                Text(
                    text = stringResource(R.string.total_item),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = uiState.total,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(weight)
                )
                Spacer(modifier = Modifier.weight(weight))
            }
            Spacer(modifier = Modifier.padding(vertical = 40.dp))
            val scope = rememberCoroutineScope()
            Button(
                onClick = {
                    scope.launch {
                        viewModel.updateShift()
                        navigateBack()
                    }
                },
                modifier = Modifier.width(100.dp)
            ) {
                Text(
                    text = stringResource(R.string.save_button),
                    fontSize = 15.sp
                )
            }
            Spacer(modifier = Modifier.padding(vertical = 10.dp))
            Button(
                onClick = { openDeleteDialog = true },
                modifier = Modifier.width(100.dp)
            ) {
                Text(
                    text = stringResource(R.string.delete_button),
                    fontSize = 15.sp
                )
            }

            if (openBreakDialog) {
                AlertDialog(
                    onDismissRequest = { openBreakDialog = false },
                    title = {
                        Text(text = stringResource(R.string.break_in_minutes))
                    },
                    text = {
                        TextField(
                            value = breakText,
                            onValueChange = { newText ->
                                breakText = newText.filter { it.isDigit() }
                            },
                            placeholder = { Text(stringResource(R.string.set_break_time)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                openBreakDialog = false
                                viewModel.updateBreakTotal(breakText)
                                viewModel.updateTotal()
                            }
                        ) {
                            Text(stringResource(R.string.confirm))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { openBreakDialog = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }

            if(openDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { openDeleteDialog = false },
                    title = {
                        Text(text = stringResource(R.string.delete_shift_dialog))
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                openDeleteDialog = false
                                scope.launch {
                                    viewModel.deleteShift()
                                    navigateBack()
                                }
                            }
                        ) {
                            Text(stringResource(R.string.confirm))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { openDeleteDialog = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }
        }
    }
}

private fun getHourAndMinute(str: String): Pair<Int, Int> {
    val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
    val time = LocalTime.parse(str, formatter)
    return Pair(time.hour, time.minute)
}