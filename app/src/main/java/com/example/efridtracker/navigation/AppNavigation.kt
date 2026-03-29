package com.example.efridtracker.navigation

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.efridtracker.rfid.RfidManager
import com.example.efridtracker.ui.location.LocationScreen
import com.example.efridtracker.ui.location.LocationViewModel
import com.example.efridtracker.ui.scan.ScanScreen
import com.example.efridtracker.ui.scan.ScanViewModelFactory

@Composable
fun AppNavigation(rfidManager: RfidManager) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "location") {

        composable("location") {
            val viewModel: LocationViewModel = viewModel()
            LocationScreen(
                viewModel = viewModel,
                onNavigateToScan = { location ->
                    navController.navigate("scan/${Uri.encode(location)}")
                }
            )
        }

        composable("scan/{location}") { backStackEntry ->
            val location = backStackEntry.arguments?.getString("location") ?: ""
            val application = LocalContext.current.applicationContext as Application
            val viewModel = viewModel<com.example.efridtracker.ui.scan.ScanViewModel>(
                factory = ScanViewModelFactory(application, location, rfidManager)
            )
            ScanScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
