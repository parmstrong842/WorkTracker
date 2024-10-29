package com.example.worktracker

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.worktracker.Constants.CLOCKED_IN_KEY
import com.example.worktracker.Constants.ON_BREAK_KEY
import com.example.worktracker.Constants.PREFS_FILE_NAME
import com.example.worktracker.ui.WorkNavGraph
import com.example.worktracker.ui.theme.WorkTrackerTheme


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WorkTrackerTheme {
                WorkNavGraph()
            }
        }
    }
}
