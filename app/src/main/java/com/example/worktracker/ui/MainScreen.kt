package com.example.worktracker.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.worktracker.AppViewModelProvider
import com.example.worktracker.MyNotification
import kotlinx.coroutines.launch

@OptIn(ExperimentalLifecycleComposeApi::class, ExperimentalMaterial3Api::class)
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

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val showMenu = remember { mutableStateOf(false) }
    Scaffold(snackbarHost = {
        SnackbarHost(snackbarHostState) { data ->
            Snackbar(
                modifier = Modifier.padding(horizontal = 80.dp, vertical = 10.dp),
                dismissAction = {
                    IconButton(onClick = { data.dismiss() }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Dismiss snackbar")
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
                title = { Text("Work Tracker") },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu.value = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Settings"
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu.value,
                            onDismissRequest = { showMenu.value = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(text = "Settings") },
                                onClick = {
                                    showMenu.value = false
                                    navigateToSettings()
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
                        text = uiState.counter
                    )
                    Spacer(modifier = Modifier.padding(vertical = 30.dp))
                    Row(
                        modifier = Modifier.width(250.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            fontSize = 20.sp,
                            text = "Shift Start "
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
                            text = "Break Start",
                            modifier = Modifier.weight(1f)
                        )
                        AnimatedVisibility(visible = uiState.onBreak) {
                            Text(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                text = uiState.breakStartTime
                            )
                        }
                        val padding by animateDpAsState(if (uiState.onBreak) 0.dp else 35.dp)
                        IconButton(
                            onClick = {
                                if (!uiState.onBreak) {
                                    MyNotification().fireNotification(
                                        context,
                                        "On Break",
                                        "You are on break"
                                    )
                                } else {
                                    MyNotification().fireNotification(
                                        context,
                                        "Clocked In",
                                        "You are clocked in"
                                    )
                                }

                                viewModel.updateOnBreak() },
                            modifier = Modifier.padding(end = padding)
                        ) {
                            Icon(
                                imageVector = if (uiState.onBreak) Icons.Default.Close else Icons.Default.PlayArrow,
                                contentDescription = "Start Break"
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
                            text = "Break Total"
                        )
                        Text(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            text = uiState.breakCounter
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
                    if (!uiState.clockedIn) {
                        MyNotification().fireNotification(
                            context,
                            "Clocked In",
                            "You are clocked in"
                        )
                    } else {
                        MyNotification().cancelNotification(context)
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                SnackbarVisualsImpl("Shift Saved")
                            )
                        }
                    }
                    viewModel.updateClockedIn()
                },
                modifier = Modifier.width(140.dp)
            ) {
                Text(
                    text = if (!uiState.clockedIn) "Clock In" else "Clock Out",
                    fontSize = 15.sp
                )
            }
            Spacer(Modifier.padding(20.dp))
            Button(
                onClick = viewShiftsOnClick,
                modifier = Modifier.width(140.dp)
            ) {
                Text(
                    text = "View Shifts",
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
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission Accepted: Do something
            Log.d("MainContent","PERMISSION GRANTED")

        } else {
            // Permission Denied: Do something
            Log.d("MainContent","PERMISSION DENIED")
        }
    }

    when (PackageManager.PERMISSION_GRANTED) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) -> {
            // Some works that require permission
            //Log.d("MainContent","has POST_NOTIFICATION permission")
        }
        else -> {
            SideEffect {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}