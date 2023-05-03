package com.example.worktracker

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.worktracker.ui.*

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            MainViewModel(
                workTrackerApplication().container.shiftsRepository,
                workTrackerApplication().container.sharedPreferencesRepository
            )
        }
        initializer {
            LogViewModel(
                workTrackerApplication().container.shiftsRepository,
                workTrackerApplication().container.sharedPreferencesRepository
            )
        }
        initializer {
            ShiftViewModel(
                workTrackerApplication().container.shiftsRepository,
                workTrackerApplication().container.sharedPreferencesRepository
            )
        }
        initializer {
            ShiftEditViewModel(
                this.createSavedStateHandle(),
                workTrackerApplication().container.shiftsRepository,
                workTrackerApplication().container.sharedPreferencesRepository
            )
        }
        initializer {
            SettingsViewModel(
                workTrackerApplication().container.sharedPreferencesRepository
            )
        }
    }
}

/**
 * Extension function to queries for [Application] object and returns an instance of
 * [WorkTrackerApplication].
 */
fun CreationExtras.workTrackerApplication(): WorkTrackerApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as WorkTrackerApplication)