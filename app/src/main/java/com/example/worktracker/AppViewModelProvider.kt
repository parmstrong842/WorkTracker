package com.example.worktracker

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.worktracker.ui.LogViewModel
import com.example.worktracker.ui.MainViewModel
import com.example.worktracker.ui.ShiftEditViewModel
import com.example.worktracker.ui.ShiftViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            MainViewModel(workTrackerApplication(),workTrackerApplication().container.shiftsRepository)
        }
        initializer {
            LogViewModel(workTrackerApplication().container.shiftsRepository)
        }
        initializer {
            ShiftViewModel(workTrackerApplication().container.shiftsRepository)
        }
        initializer {
            ShiftEditViewModel(
                this.createSavedStateHandle(),
                workTrackerApplication().container.shiftsRepository
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