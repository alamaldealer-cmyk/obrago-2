package com.obrago.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.obrago.app.data.model.Role
import com.obrago.app.ui.auth.AuthMode
import com.obrago.app.ui.auth.AuthNavTarget
import com.obrago.app.ui.auth.AuthViewModel
import com.obrago.app.ui.auth.ForgotPasswordScreen
import com.obrago.app.ui.auth.LoginScreen
import com.obrago.app.ui.auth.RegisterCustomerScreen
import com.obrago.app.ui.auth.RegisterWorkerScreen
import com.obrago.app.ui.auth.RoleSelectionScreen
import com.obrago.app.ui.admin.AdminRoot
import com.obrago.app.ui.customer.CustomerRoot
import com.obrago.app.ui.customer.ProfileScreen
import com.obrago.app.ui.notifications.NotificationsBridge
import com.obrago.app.ui.worker.WorkerRoot

@Composable
fun ObragoNavGraph(navController: NavHostController = rememberNavController()) {
    // Shared across all auth screens, same idea as useAppStore() in the web app
    val authViewModel: AuthViewModel = viewModel()
    val state by authViewModel.uiState.collectAsState()

    // App-wide admin broadcast listener, mirrors sendAdminBroadcast() delivery
    // to whichever role is currently signed in (see notificationService.ts).
    val notificationsBridge: NotificationsBridge = viewModel()
    val latestBroadcast by notificationsBridge.latestBroadcast.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(latestBroadcast?.id) {
        val broadcast = latestBroadcast
        if (broadcast != null) {
            com.obrago.app.notifications.NotificationHelper.showNotification(
                context, broadcast.title, broadcast.message
            )
            notificationsBridge.consume()
        }
    }

    // React to one-shot navigation events fired by AuthViewModel (login success, register success...)
    androidx.compose.runtime.LaunchedEffect(state.navigateTo) {
        when (state.navigateTo) {
            AuthNavTarget.HOME_CUSTOMER -> navController.navigate(Routes.HOME_CUSTOMER) { popUpTo(0) { inclusive = true } }
            AuthNavTarget.HOME_WORKER -> navController.navigate(Routes.HOME_WORKER) { popUpTo(0) { inclusive = true } }
            AuthNavTarget.ADMIN_PANEL -> navController.navigate(Routes.ADMIN_PANEL) { popUpTo(0) { inclusive = true } }
            null -> Unit
        }
        if (state.navigateTo != null) authViewModel.consumeNavigation()
    }

    NavHost(navController = navController, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) {
            com.obrago.app.ui.splash.SplashScreen(
                onSplashFinished = { targetRoute ->
                    navController.navigate(targetRoute) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.ROLE_SELECT) {
            RoleSelectionScreen(onRoleChosen = { role ->
                authViewModel.setRole(role)
                authViewModel.setAuthMode(AuthMode.LOGIN)
                navController.navigate(Routes.LOGIN)
            })
        }
        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Routes.ROLE_SELECT)
                    }
                },
                onGoRegister = {
                    val dest = if (state.role == Role.WORKER) Routes.REGISTER_WORKER else Routes.REGISTER_CUSTOMER
                    navController.navigate(dest)
                },
                onGoForgotPassword = {
                    authViewModel.setForgotPasswordMode(true)
                    navController.navigate(Routes.FORGOT_PASSWORD)
                }
            )
        }
        composable(Routes.REGISTER_CUSTOMER) {
            RegisterCustomerScreen(viewModel = authViewModel, onBack = { navController.popBackStack() })
        }
        composable(Routes.REGISTER_WORKER) {
            RegisterWorkerScreen(viewModel = authViewModel, onBack = { navController.popBackStack() })
        }
        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onDone = { navController.popBackStack() }
            )
        }

        // Placeholder destinations - built out in the next phases of this project
        composable(Routes.HOME_CUSTOMER) {
            CustomerRoot(onOpenProfile = { navController.navigate(Routes.PROFILE) })
        }
        composable(Routes.PROFILE) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onLoggedOut = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.HOME_WORKER) {
            WorkerRoot(onOpenProfile = { navController.navigate(Routes.PROFILE) })
        }
        composable(Routes.ADMIN_PANEL) {
            AdminRoot(onLogout = {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            })
        }
    }
}

@Composable
private fun PlaceholderScreen(label: String) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(label, style = MaterialTheme.typography.titleMedium)
    }
}
