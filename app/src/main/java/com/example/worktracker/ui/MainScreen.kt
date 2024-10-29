package com.example.worktracker.ui

import android.Manifest
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.worktracker.AppViewModelProvider
import com.example.worktracker.Constants
import com.example.worktracker.NotificationHandler
import com.example.worktracker.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SnackbarVisualsImpl(
    override val message: String,
) : SnackbarVisuals {
    override val actionLabel: String
        get() = ""
    override val withDismissAction: Boolean
        get() = true
    override val duration: SnackbarDuration
        get() = SnackbarDuration.Short
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewShiftsOnClick: () -> Unit = {},
    navigateToSettings: () -> Unit,
    viewModel: MainViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {

    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        CheckPermissions(context)
    }

    val scope = rememberCoroutineScope()
    val createFileLauncher = rememberLauncherForActivityResult(CreateDocument("text/csv")) { uri ->
        uri?.let {
            scope.launch {
                val shiftData = viewModel.fetchShiftData()
                val csvHeader = "id,date,shiftSpan,breakTotal,shiftTotal\n"
                val csvContent = StringBuilder(csvHeader)

                for (shift in shiftData) {
                    csvContent.append("${shift.id},${shift.date},${shift.shiftSpan},${shift.breakTotal},${shift.shiftTotal}\n")
                }
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    outputStream.write(csvContent.toString().toByteArray())
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context,
                        context.getString(R.string.file_successfully_saved), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val showMenu = remember { mutableStateOf(false) }
    Scaffold(snackbarHost = {
        SnackbarHost(snackbarHostState) { data ->
            Snackbar(
                modifier = Modifier.padding(horizontal = 80.dp, vertical = 10.dp),
                dismissAction = {
                    IconButton(onClick = { data.dismiss() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.dismiss_snackbar)
                        )
                    }
                }
            ) {
                Text(
                    text = data.visuals.message,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.main_screen_top_bar)) },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu.value = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.settings)
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu.value,
                            onDismissRequest = { showMenu.value = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(text = stringResource(R.string.settings)) },
                                onClick = {
                                    showMenu.value = false
                                    navigateToSettings()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(text = stringResource(R.string.export_csv)) },
                                onClick = {
                                    showMenu.value = false
                                    createFileLauncher.launch("shifts.csv")
                                }
                            )
                        }
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            AnimatedVisibility(visible = uiState.clockedIn) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        text = uiState.counter,
                        modifier = Modifier.testTag("counter")
                    )
                    Spacer(modifier = Modifier.padding(vertical = 30.dp))
                    Row(
                        modifier = Modifier.width(250.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            fontSize = 20.sp,
                            text = stringResource(R.string.shift_start)
                        )
                        Text(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            text = uiState.shiftStartTime
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .width(346.dp)
                            .padding(start = 47.dp),
                    ) {
                        Text(
                            fontSize = 20.sp,
                            text = stringResource(R.string.break_start),
                            modifier = Modifier.weight(1f)
                        )
                        AnimatedVisibility(visible = uiState.onBreak) {
                            Text(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                text = uiState.breakStartTime
                            )
                        }
                        val padding by animateDpAsState(if (uiState.onBreak) 0.dp else 35.dp,
                            label = ""
                        )
                        IconButton(
                            onClick = {
                                val onBreak = uiState.onBreak
                                viewModel.updateOnBreak()
                                if (!onBreak) {
                                    NotificationHandler.fireBreakNotification(context)
                                } else {
                                    NotificationHandler.fireNotification(context)
                                }
                            },
                            modifier = Modifier.padding(end = padding)
                        ) {
                            Icon(
                                imageVector = if (uiState.onBreak) Icons.Default.Close else Icons.Default.PlayArrow,
                                contentDescription = stringResource(R.string.break_start)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.width(250.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            fontSize = 20.sp,
                            text = stringResource(R.string.break_total)
                        )
                        Text(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            text = uiState.breakCounter,
                            modifier = Modifier.testTag("breakCounter")
                        )
                    }
                }
            }
            val p = 40.dp
            Spacer(Modifier.padding(p))
            Divider(Modifier.fillMaxWidth(0.85f), thickness = 3.dp)
            Spacer(Modifier.padding(p))
            Button(
                onClick = {
                    val clockedIn = uiState.clockedIn
                    viewModel.updateClockedIn()
                    if (!clockedIn) {
                        NotificationHandler.startRecurringNotification(context)
                    } else {
                        NotificationHandler.endRecurringNotification(context)
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                SnackbarVisualsImpl(context.getString(R.string.shift_saved))
                            )
                        }
                    }
                },
                modifier = Modifier.width(140.dp)
            ) {
                Text(
                    text = if (!uiState.clockedIn)
                        stringResource(R.string.clock_in)
                    else stringResource(R.string.clock_out),
                    fontSize = 15.sp
                )
            }
            Spacer(Modifier.padding(20.dp))
            Button(
                onClick = viewShiftsOnClick,
                modifier = Modifier.width(140.dp)
            ) {
                Text(
                    text = stringResource(R.string.view_shifts),
                    fontSize = 15.sp
                )
            }
            Spacer(Modifier.padding(p))
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun CheckPermissions(context: Context) {
    val sharedPref = context.getSharedPreferences(Constants.PREFS_FILE_NAME, Context.MODE_PRIVATE)
    if (!sharedPref.getBoolean(Constants.CHECKED_FOR_PERMISSIONS_KEY, false)) {
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) {
            sharedPref.edit().putBoolean(Constants.CHECKED_FOR_PERMISSIONS_KEY, true).apply()
        }

        SideEffect {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}