package com.example.worktracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.worktracker.Constants.TIME_ZONE_KEY
import com.example.worktracker.data.SharedPreferencesRepository
import kotlinx.coroutines.launch

class SettingsViewModel(private val sharedPreferencesRepository: SharedPreferencesRepository) : ViewModel() {

    fun updateTimeZone(newTimeZone: String) {
        viewModelScope.launch {
            sharedPreferencesRepository.putString(TIME_ZONE_KEY, newTimeZone)
        }
    }

    fun updateStartOfWeek(newStartOfWeek: String) {
        viewModelScope.launch {
            sharedPreferencesRepository.putString(TIME_ZONE_KEY, newStartOfWeek)
        }
    }
}