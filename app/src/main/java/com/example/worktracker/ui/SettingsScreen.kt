package com.example.worktracker.ui


import android.view.MotionEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.worktracker.AppViewModelProvider
import com.example.worktracker.TimeZoneInfo.letterToIndexMap
import com.example.worktracker.TimeZoneInfo.timeZoneList
import com.example.worktracker.ui.theme.LocalIsDarkMode
import com.example.worktracker.ui.theme.blue2
import com.example.worktracker.ui.theme.blue3
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navigateBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

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
        content = { paddingValues ->
            Column(
                modifier = Modifier.padding(
                    top = paddingValues.calculateTopPadding() + 16.dp,
                    bottom = paddingValues.calculateBottomPadding() + 16.dp,
                    start = paddingValues.calculateStartPadding(LocalLayoutDirection.current) + 16.dp,
                    end = paddingValues.calculateEndPadding(LocalLayoutDirection.current) + 16.dp
                )
            ) {
                TimeZoneSettings(
                    selectedItem = uiState.timeZoneDisplay,
                    onItemSelected = { timeZone ->
                        viewModel.updateTimeZone(timeZone)
                    },
                    setColumnHeight = { viewModel.columnHeight = it },
                    setTouchPositionY = { viewModel.setTouchPositionY(it) },
                    selectedLetter = uiState.selectedLetter,
                    touchPositionY = uiState.touchPositionY
                )
                WeekSettings(
                    selectedItem = uiState.startOfWeek,
                    onItemSelected = { startOfWeek ->
                        viewModel.updateStartOfWeek(startOfWeek)
                    }
                )
            }
        }
    )
}

@Composable
fun TimeZoneSettings(
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    setColumnHeight: (Float) -> Unit,
    setTouchPositionY: (Float?) -> Unit,
    selectedLetter: String?,
    touchPositionY: Float?
) {
    val showDialog = remember { mutableStateOf(false) }

    SettingsItem(
        settingName = "Time Zone",
        selectedItem = selectedItem,
        onClick = { showDialog.value = true },
    )

    TimeZoneSelectionDialog(
        showDialog = showDialog.value,
        onDismissRequest = { showDialog.value = false },
        onTimeZoneSelected = onItemSelected,
        setColumnHeight = setColumnHeight,
        setTouchPositionY = setTouchPositionY,
        selectedLetter = selectedLetter,
        touchPositionY = touchPositionY
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TimeZoneSelectionDialog(
    showDialog: Boolean,
    onDismissRequest: () -> Unit,
    onTimeZoneSelected: (String) -> Unit,
    setColumnHeight: (Float) -> Unit,
    setTouchPositionY: (Float?) -> Unit,
    selectedLetter: String?,
    touchPositionY: Float?
) {
    if (showDialog) {
        val searchQuery = remember { mutableStateOf("") }
        val listState = rememberLazyListState()

        LaunchedEffect(searchQuery.value) {
            listState.scrollToItem(0)
        }

        AlertDialog(
            onDismissRequest = onDismissRequest,
            text = {
                val keyboardController = LocalSoftwareKeyboardController.current

                LaunchedEffect(listState) {
                    snapshotFlow { listState.isScrollInProgress }.collect { isScrolling ->
                        if (isScrolling) {
                            keyboardController?.hide()
                        }
                    }
                }

                selectedLetter?.let { letter ->
                    LaunchedEffect(letter) {
                        letterToIndexMap[selectedLetter]?.let { listState.scrollToItem(it) }
                        keyboardController?.hide()
                    }
                }

                Column(Modifier.fillMaxSize()) {
                    SearchBar(onDismissRequest, searchQuery)
                    Spacer(modifier = Modifier.padding(8.dp))
                    Box {
                        Row {
                            TimeZoneLazyColumn(listState, searchQuery, onTimeZoneSelected, onDismissRequest, Modifier.weight(1f))
                            if (searchQuery.value.isEmpty()) {
                                ABCBar(setColumnHeight, setTouchPositionY, touchPositionY)
                            }
                        }
                        SelectedLetterDisplay(selectedLetter, touchPositionY, Modifier.align(Alignment.CenterEnd))
                        ScrollToTopButton(listState, Modifier.align(Alignment.BottomCenter))
                    }
                }
            },
            shape = RectangleShape,
            containerColor = if (LocalIsDarkMode.current) Color.Black else blue3,
            confirmButton = {},
            properties = DialogProperties(usePlatformDefaultWidth = false)
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SearchBar(
    onDismissRequest: () -> Unit,
    searchQuery: MutableState<String>
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onDismissRequest) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }
        OutlinedTextField(
            value = searchQuery.value,
            onValueChange = { searchQuery.value = it },
            placeholder = { Text("Search") },
            trailingIcon = {
                if (searchQuery.value.isNotEmpty()) {
                    IconButton(onClick = { searchQuery.value = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "clear text"
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun TimeZoneLazyColumn(
    listState: LazyListState,
    searchQuery: MutableState<String>,
    onTimeZoneSelected: (String) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        state = listState
    ) {
        val filteredTimeZoneList = timeZoneList.filter {
            it.second.contains(searchQuery.value, ignoreCase = true)
        }
        items(filteredTimeZoneList, { it.first }) { timeZone ->
            TimeZoneItem(
                timeZoneText = timeZone.second,
                offsetText = timeZone.third,
                onItemSelected = {
                    onTimeZoneSelected(timeZone.first)
                    onDismissRequest()
                }
            )
        }
    }
}

@Composable
fun TimeZoneItem(
    modifier: Modifier = Modifier,
    timeZoneText: String,
    offsetText: String,
    onItemSelected: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Release -> {
                    delay(100)
                    onItemSelected()
                }
                else -> {}
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(),
                onClick = {}
            )
    ) {
        Text(
            text = timeZoneText,
            modifier = Modifier.padding(top = 8.dp),
            fontSize = 20.sp
        )
        Text(
            text = offsetText,
            modifier = Modifier.padding(bottom = 8.dp),
            color = Color.Gray,
            fontSize = 17.sp
        )
        Divider()
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
private fun ABCBar(
    setColumnHeight: (Float) -> Unit,
    setTouchPositionY: (Float?) -> Unit,
    touchPositionY: Float?
) {
    val sideBarColor = if (LocalIsDarkMode.current) Color.DarkGray else Color.LightGray
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(50))
            .background(sideBarColor)
            .onGloballyPositioned { coordinates ->
                setColumnHeight(coordinates.size.height.toFloat())

            }
            .pointerInteropFilter { event ->
                when (event.action) {
                    MotionEvent.ACTION_UP -> {
                        setTouchPositionY(null)
                    }
                    else -> {
                        setTouchPositionY(event.y)
                    }
                }
                true
            }
            .drawBehind {
                if (touchPositionY != null) {
                    drawRoundRect(
                        color = Color.Gray,
                        topLeft = Offset(
                            0f,
                            touchPositionY - (30.dp.toPx() / 2)
                        ),
                        size = Size(size.width, 30.dp.toPx()),
                        cornerRadius = CornerRadius(10.dp.toPx())
                    )
                }
            },
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        val dotColor = if (LocalIsDarkMode.current) Color.LightGray else Color.Black
        repeat(26) {
            val barWidth = 13.dp
            Canvas(modifier = Modifier.size(barWidth)) {
                drawCircle(
                    color = dotColor,
                    radius = 2.dp.toPx(),
                    center = Offset(
                        barWidth.toPx() / 2,
                        barWidth.toPx() / 2
                    )
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalTextApi::class)
private fun SelectedLetterDisplay(
    selectedLetter: String?,
    touchPositionY: Float?,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    Spacer(
        modifier = modifier
            .fillMaxHeight()
            .width(75.dp)
            .drawWithCache {
                val measuredText =
                    textMeasurer.measure(
                        text = AnnotatedString(selectedLetter ?: ""),
                        style = TextStyle(fontSize = 40.sp)
                    )

                onDrawBehind {
                    if (touchPositionY != null) {
                        drawCircle(
                            color = blue2,
                            radius = 30.dp.toPx(),
                            center = Offset(0f, touchPositionY)
                        )

                        drawText(
                            textLayoutResult = measuredText,
                            topLeft = Offset(
                                -measuredText.size.width.toFloat() / 2,
                                touchPositionY - measuredText.size.height.toFloat() / 2
                            )
                        )
                    }
                }
            }
    )
}

@Composable
private fun ScrollToTopButton(
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    val isScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemScrollOffset // It doesn't work if this isn't here. I have know idea why
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
        }
    }
    Box(modifier) {
        AnimatedVisibility(
            visible = isScrolled,
            enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
            exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
        ) {
            val coroutineScope = rememberCoroutineScope()

            val customColors = if (LocalIsDarkMode.current)
                IconButtonDefaults.iconButtonColors(
                    containerColor = Color.DarkGray.copy(alpha = 0.75f),
                    contentColor = Color.LightGray
                )
            else
                IconButtonDefaults.iconButtonColors(
                    containerColor = Color.LightGray.copy(alpha = 0.75f),
                    contentColor = Color.Black
                )

            IconButton(
                onClick = {
                    coroutineScope.launch {
                        listState.animateScrollToItem(0)
                    }
                },
                modifier = Modifier
                    .padding(bottom = 16.dp),
                colors = customColors
            ) {
                Icon(
                    imageVector = Icons.Default.VerticalAlignTop,
                    contentDescription = "go to top"
                )
            }
        }
    }
}

@Composable
fun WeekSettings(
    selectedItem: String,
    onItemSelected: (String) -> Unit,
) {
    val showDialog = remember { mutableStateOf(false) }

    SettingsItem(
        settingName = "Start of Week",
        selectedItem = selectedItem,
        onClick = { showDialog.value = true },
    )

    WeekSelectionDialog(
        showDialog = showDialog.value,
        onDismissRequest = { showDialog.value = false },
        onStartOfWeekSelected = onItemSelected
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
                    WeekItem(day = "Saturday", onDismissRequest, onStartOfWeekSelected)
                    Divider(color = Color.Black)
                    WeekItem(day = "Sunday", onDismissRequest, onStartOfWeekSelected)
                    Divider(color = Color.Black)
                    WeekItem(day = "Monday", onDismissRequest, onStartOfWeekSelected)
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
fun WeekItem(
    day: String,
    onDismissRequest: () -> Unit,
    onStartOfWeekSelected: (String) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Release -> {
                    delay(100)
                    onDismissRequest()
                }
                else -> {}
            }
        }
    }
    Box(
        modifier = Modifier
            .height(50.dp)
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(),
                onClick = { onStartOfWeekSelected(day.uppercase()) }
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = day,
            fontSize = 20.sp
        )
    }
}

@Composable
fun SettingsItem(
    settingName: String,
    selectedItem: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.clickable(onClick = onClick)
    ){
        Column {
            Text(
                text = settingName,
                modifier = Modifier.padding(top = 8.dp),
                fontSize = 20.sp
            )
            Text(
                text = selectedItem,
                modifier = Modifier.padding(bottom = 8.dp),
                color = Color.Gray,
                fontSize = 17.sp
            )
            Divider()
        }
    }
}