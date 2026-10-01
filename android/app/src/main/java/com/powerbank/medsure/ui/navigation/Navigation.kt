package com.powerbank.medsure.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.powerbank.medsure.data.session.SessionManager
import com.powerbank.medsure.ui.screens.common.LoginScreen
import com.powerbank.medsure.ui.screens.common.SplashScreen
import com.powerbank.medsure.ui.screens.family.FamilyHomeScreen
import com.powerbank.medsure.ui.screens.hospital.*
import com.powerbank.medsure.ui.screens.patient.*
import com.powerbank.medsure.ui.theme.BrandNavy
import com.powerbank.medsure.ui.theme.BrandNavyDark
import com.powerbank.medsure.ui.theme.SurfaceCard
import com.powerbank.medsure.viewmodel.*

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Splash : Screen("splash", "Splash", Icons.Default.LocalHospital)
    object Login : Screen("login", "Login", Icons.Default.Lock)

    // Patient
    object PatientHome : Screen("patient_home", "Home", Icons.Default.Home)
    object Discharge : Screen("discharge", "Discharge", Icons.Default.Description)
    object Bills : Screen("bills", "Bills", Icons.Default.ReceiptLong)
    object Insurance : Screen("insurance", "Insurance", Icons.Default.Security)
    object CheckIn : Screen("checkin", "Check-in", Icons.Default.MedicalServices)

    // Family
    object FamilyHome : Screen("family_home", "Home", Icons.Default.Home)

    // Hospital
    object HospitalHome : Screen("hospital_home", "Overview", Icons.Default.Dashboard)
    object HospitalDischarges : Screen("hospital_discharges", "Discharges", Icons.Default.UploadFile)
    object HospitalQueries : Screen("hospital_queries", "Queries", Icons.Default.QuestionAnswer)
    object HospitalAlerts : Screen("hospital_alerts", "Alerts", Icons.Default.Emergency)
}

@Composable
fun MedSureNavHost() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val dischargeViewModel: DischargeViewModel = viewModel()
    val billViewModel: BillViewModel = viewModel()
    val insuranceViewModel: InsuranceViewModel = viewModel()
    val checkInViewModel: CheckInViewModel = viewModel()
    val hospitalViewModel: HospitalViewModel = viewModel()

    val currentUser by SessionManager.currentUser.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isAuthScreen = currentRoute == Screen.Splash.route || currentRoute == Screen.Login.route

    // Determine role-based navigation bar items
    val bottomNavItems = when (currentUser?.role) {
        "HOSPITAL_DESK" -> listOf(
            Screen.HospitalHome,
            Screen.HospitalDischarges,
            Screen.HospitalQueries,
            Screen.HospitalAlerts
        )
        "FAMILY" -> listOf(
            Screen.FamilyHome,
            Screen.Discharge,
            Screen.Bills,
            Screen.Insurance,
            Screen.CheckIn
        )
        else -> listOf(
            Screen.PatientHome,
            Screen.Discharge,
            Screen.Bills,
            Screen.Insurance,
            Screen.CheckIn
        )
    }

    Scaffold(
        bottomBar = {
            if (!isAuthScreen && currentUser != null) {
                NavigationBar(
                    containerColor = SurfaceCard,
                    tonalElevation = NavigationBarDefaults.Elevation
                ) {
                    bottomNavItems.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = selected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = BrandNavy,
                                selectedTextColor = BrandNavy,
                                indicatorColor = BrandNavy.copy(alpha = 0.12f)
                            ),
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onTimeout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = { role ->
                        val targetRoute = when (role) {
                            "HOSPITAL_DESK" -> Screen.HospitalHome.route
                            "FAMILY" -> Screen.FamilyHome.route
                            else -> Screen.PatientHome.route
                        }
                        navController.navigate(targetRoute) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            // Patient Routes
            composable(Screen.PatientHome.route) {
                PatientHomeScreen(
                    onNavigateToDischarge = { navController.navigate(Screen.Discharge.route) },
                    onNavigateToBills = { navController.navigate(Screen.Bills.route) },
                    onNavigateToInsurance = { navController.navigate(Screen.Insurance.route) },
                    onNavigateToCheckIn = { navController.navigate(Screen.CheckIn.route) },
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Discharge.route) {
                DischargeScreen(viewModel = dischargeViewModel)
            }

            composable(Screen.Bills.route) {
                BillScreen(viewModel = billViewModel)
            }

            composable(Screen.Insurance.route) {
                InsuranceScreen(viewModel = insuranceViewModel)
            }

            composable(Screen.CheckIn.route) {
                CheckInScreen(viewModel = checkInViewModel)
            }

            // Family Route
            composable(Screen.FamilyHome.route) {
                FamilyHomeScreen(
                    onNavigateToDischarge = { navController.navigate(Screen.Discharge.route) },
                    onNavigateToBills = { navController.navigate(Screen.Bills.route) },
                    onNavigateToInsurance = { navController.navigate(Screen.Insurance.route) },
                    onNavigateToCheckIn = { navController.navigate(Screen.CheckIn.route) },
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // Hospital Desk Routes
            composable(Screen.HospitalHome.route) {
                HospitalHomeScreen(
                    viewModel = hospitalViewModel,
                    onNavigateToDischarges = { navController.navigate(Screen.HospitalDischarges.route) },
                    onNavigateToQueries = { navController.navigate(Screen.HospitalQueries.route) },
                    onNavigateToAlerts = { navController.navigate(Screen.HospitalAlerts.route) },
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.HospitalDischarges.route) {
                DischargesScreen(
                    viewModel = hospitalViewModel,
                    onViewPatientPack = { navController.navigate(Screen.Discharge.route) }
                )
            }

            composable(Screen.HospitalQueries.route) {
                QueriesScreen(viewModel = hospitalViewModel)
            }

            composable(Screen.HospitalAlerts.route) {
                AlertsScreen(viewModel = hospitalViewModel)
            }
        }
    }
}
