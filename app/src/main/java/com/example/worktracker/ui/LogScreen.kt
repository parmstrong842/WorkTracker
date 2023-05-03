package com.example.worktracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.worktracker.AppViewModelProvider
import com.example.worktracker.data.Shift
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogScreen(
    navigateBack: () -> Unit,
    navigateToShift: () -> Unit = {},
    navigateToItemUpdate: (Int) -> Unit,
    viewModel: LogViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val titles = listOf("Week", "Month", "Year", "All")

    val total = getTotal(uiState.itemList)

    Scaffold(
        topBar = {
            LogTopAppBar(
                title = "Shifts",
                navigateBack = navigateBack,
                createShift = navigateToShift
            )
        },
        bottomBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.secondary)) {
                Divider()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(50.dp)
                ) {
                    Text(
                        text = "Total: ",
                        textAlign = TextAlign.Right,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(2f),
                        fontSize = 18.sp

                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = total,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        fontSize = 18.sp
                    )
                }
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier.padding(horizontal = 5.dp),
                    color = MaterialTheme.colorScheme.tertiary
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)
                    ) {
                        Button(
                            onClick = { viewModel.minusDate() },
                            modifier =  Modifier.fillMaxHeight(),
                            shape = RoundedCornerShape(topStart = 25.dp, bottomStart = 25.dp),
                        ) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                        }
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            if (uiState.tabState == 3) {
                                Text(
                                    text = "All Shifts",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            } else {
                                Text(
                                    text = "${uiState.startDate} -",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "${uiState.endDate.minusDays(1)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                        }
                        Button(
                            onClick = { viewModel.plusDate() },
                            modifier =  Modifier.fillMaxHeight(),
                            shape = RoundedCornerShape(topEnd = 25.dp, bottomEnd = 25.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null
                            )
                        }
                    }
                }
                Spacer(Modifier.padding(10.dp))
            }
        }
    ) {
        Column(Modifier.padding(it)) {
            TabRow(selectedTabIndex = uiState.tabState) {
                titles.forEachIndexed { index, title ->
                    Tab(
                        selected = uiState.tabState == index,
                        onClick = { viewModel.updateTabState(index) },
                        text = { Text(text = title) }
                    )
                }
            }
            Spacer(Modifier.padding(5.dp))
            ShiftHeader()
            Spacer(Modifier.padding(5.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(uiState.itemList) { item ->
                    ShiftItem(item, navigateToItemUpdate)
                }
            }
        }
    }
}

@Composable
fun ShiftHeader() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Date",
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(2f)
        )
        Text(
            text = "Break",
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "Time",
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ShiftItem(
    item: Shift,
    onItemClick: (Int) -> Unit,
) {
    Column {
        Divider()
        Spacer(Modifier.padding(vertical = 2.dp))
        Row(
            modifier = Modifier
                .clickable { onItemClick(item.id) }
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(2f)
            ) {
                val date = LocalDate.parse(item.date, DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                Text(DateTimeFormatter.ofPattern("EEE, LLL d").format(date))
                Text(item.shiftSpan)
            }
            Text(
                text = item.breakTotal,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = item.shiftTotal,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit = {},
    createShift: () -> Unit = {}
) {
    TopAppBar(
        title = { Text(title) },
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = navigateBack) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            IconButton(onClick = createShift) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Create new shift"
                )
            }
        }
    )
}

fun getTotal(list: List<Shift>): String {
    var seconds = 0
    list.forEach {
        val tokens = it.shiftTotal.split(':')
        seconds += tokens[0].toInt() * 60 * 60
        seconds += tokens[1].toInt() * 60
    }
    val minutes = seconds / (60) % 60
    val hours = seconds / (60 * 60)

    return String.format("%d:%02d", hours, minutes)//TODO might have issue with negatives
}

@Preview(widthDp = 300)
@Composable
fun ItemPreview() {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            ShiftHeader()
        }
        items(listOf(Shift(0, "2023.01.22", "2023.01.22.10.36", "10:36", "0:00"))) { item ->
            ShiftItem(item) {}
        }
    }
}