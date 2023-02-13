package com.example.worktracker.ui


import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

enum class Screen {
    Main,
    Log,
    Shift,
}

@Composable
fun WorkNavGraph(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Main.name,
        modifier = modifier
    ) {
        composable(route = Screen.Main.name) {
            MainScreen(
                viewShiftsOnClick = { navController.navigate(Screen.Log.name) }
            )
        }
        composable(route = Screen.Log.name) {
            LogScreen(
                navigateToShift = { navController.navigate(Screen.Shift.name) },
                navigateUp = { navController.popBackStack() },
                navigateToItemUpdate = {
                    navController.navigate("${ShiftEditDestination.route}/${it}")
                }
            )
        }
        composable(route = Screen.Shift.name) {
            ShiftScreen(
                topBarTitle = "New Shift",
                navigateBack = {
                    navController.getBackStackEntry(Screen.Log.name).viewModelStore.clear()
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
                topBarTitle = "Edit Shift",
                navigateBack = {
                    navController.getBackStackEntry(Screen.Log.name).viewModelStore.clear()
                    navController.popBackStack()
                }
            )
        }
    }
}