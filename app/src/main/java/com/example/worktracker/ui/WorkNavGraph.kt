package com.example.worktracker.ui


import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.worktracker.R

enum class Screen {
    MainScreen,
    LogScreen,
    ShiftScreen,
    SettingsScreen,
}

@Composable
fun WorkNavGraph(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.MainScreen.name,
        modifier = modifier
    ) {
        composable(route = Screen.MainScreen.name) {
            MainScreen(
                viewShiftsOnClick = { navController.navigate(Screen.LogScreen.name) },
                navigateToSettings = { navController.navigate(Screen.SettingsScreen.name) }
            )
        }
        composable(route = Screen.LogScreen.name) {
            LogScreen(
                navigateToShift = { navController.navigate(Screen.ShiftScreen.name) },
                navigateBack = { navController.popBackStack() },
                navigateToItemUpdate = {
                    navController.navigate("${ShiftEditDestination.route}/$it")
                }
            )
        }
        composable(route = Screen.ShiftScreen.name) {
            ShiftScreen(
                topBarTitle = stringResource(R.string.new_shift),
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = ShiftEditDestination.routeWithArgs,
            arguments = listOf(navArgument(ShiftEditDestination.shiftIdArg) {
                type = NavType.IntType
            })
        ) {
            ShiftEditScreen(
                topBarTitle = stringResource(R.string.edit_shift),
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(route = Screen.SettingsScreen.name) {
            SettingsScreen(
                navigateBack = {
                    navController.getBackStackEntry(Screen.MainScreen.name).viewModelStore.clear()
                    navController.popBackStack()
                },
            )
        }
    }
}