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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.worktracker.AppViewModelProvider
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object ShiftEditDestination {
    const val route = "shift_edit"
    const val shiftIdArg = "shiftId"
    const val routeWithArgs = "$route/{$shiftIdArg}"
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ShiftEditScreen(
    topBarTitle: String,
    navigateBack: () -> Unit,
    viewModel: ShiftEditViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var openDialog by rememberSaveable { mutableStateOf(false) }
    var breakText by rememberSaveable { mutableStateOf("") }

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
                    text = "Shift Start:",
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
                        fontSize = 15.sp
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
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center

                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select start time"
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
                    text = "Shift End:",
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
                        fontSize = 15.sp
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
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select end time"
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
                    text = "Break:",
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = uiState.breakTotal,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(weight)
                )
                TextButton(
                    onClick = {
                        openDialog = true
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
                            contentDescription = "Select break time"
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
                    text = "Total:",
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
            Button(
                onClick = {
                    viewModel.updateShift()
                    navigateBack()
                },
                modifier = Modifier.width(100.dp)
            ) {
                Text(
                    text = "Save",
                    fontSize = 15.sp
                )
            }
            Spacer(modifier = Modifier.padding(vertical = 10.dp))
            Button(
                onClick = {
                    viewModel.deleteShift()
                    navigateBack()
                },
                modifier = Modifier.width(100.dp)
            ) {
                Text(
                    text = "Delete",
                    fontSize = 15.sp
                )
            }

            if (openDialog) {
                AlertDialog(
                    onDismissRequest = { openDialog = false },
                    title = {
                        Text(text = "Break (In Minutes)")
                    },
                    text = {
                        TextField(
                            value = breakText,
                            onValueChange = { breakText = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)//TODO disable comma and period on keyboard. Only some phones
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                openDialog = false
                                viewModel.updateBreakTotal(breakText)
                                viewModel.updateTotal()
                            }
                        ) {
                            Text("Confirm")
                        }
                    },
                )
            }
        }
    }
}

private fun getHourAndMinute(str: String): Pair<Int, Int> {
    val formatter = DateTimeFormatter.ofPattern("h:mm a")
    val time = LocalTime.parse(str, formatter)
    return Pair(time.hour, time.minute)
}