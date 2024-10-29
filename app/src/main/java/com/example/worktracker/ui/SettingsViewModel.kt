package com.example.worktracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.worktracker.Constants.START_OF_WEEK_KEY
import com.example.worktracker.Constants.TIME_ZONE_KEY
import com.example.worktracker.TimeZoneInfo.getTimeZoneDisplay
import com.example.worktracker.data.SharedPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val timeZoneDisplay: String,
    val startOfWeek: String,
    val selectedLetter: String?,
    val touchPositionY: Float?
)

class SettingsViewModel(private val sharedPref: SharedPreferencesRepository) : ViewModel() {

    private val _uiState: MutableStateFlow<SettingsUiState>
    val uiState: StateFlow<SettingsUiState>

    var columnHeight = 0f

    init {
        val timeZoneId = sharedPref.getString(TIME_ZONE_KEY, "Etc/UTC")
        val dayOfWeekString = sharedPref.getString(START_OF_WEEK_KEY, "SUNDAY").toProperCase()

        _uiState = MutableStateFlow(
            SettingsUiState(
                getTimeZoneDisplay(timeZoneId),
                dayOfWeekString,
                null,
                null
            )
        )
        uiState = _uiState.asStateFlow()
    }

    fun setTouchPositionY(value: Float?) {
        val y = value?.coerceIn(0f, columnHeight-1)
        val selectedLetter = calculateSelectedLetter(y)
        _uiState.update {
            it.copy(
                selectedLetter = selectedLetter?.toString(),
                touchPositionY = y
            )
        }
    }

    private fun calculateSelectedLetter(y: Float?): Char?{
        var selectedLetter: Char? = null
        if (y != null) {
            val segmentHeight = columnHeight / 26
            val segmentIndex = (y / segmentHeight).toInt()
            selectedLetter = 'A' + segmentIndex
        }
        return selectedLetter
    }

    fun updateTimeZone(newTimeZone: String) {
        viewModelScope.launch {
            sharedPref.putString(TIME_ZONE_KEY, newTimeZone)
            _uiState.update {
                it.copy(
                    timeZoneDisplay = getTimeZoneDisplay(newTimeZone)
                )
            }
        }
    }

    fun updateStartOfWeek(newStartOfWeek: String) {
        viewModelScope.launch {
            sharedPref.putString(START_OF_WEEK_KEY, newStartOfWeek)
            _uiState.update {
                it.copy(
                    startOfWeek = newStartOfWeek.toProperCase()
                )
            }
        }
    }

    private fun String.toProperCase(): String {
        return this.lowercase().replaceFirstChar { it.uppercase() }
    }
}