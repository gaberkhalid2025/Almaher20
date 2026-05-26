package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.*
import com.example.ui.theme.WamWalletTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup full-bleed edge-to-edge drawings
        enableEdgeToEdge()
        
        setContent {
            WamWalletTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackgroundVal
                ) {
                    WalletNavigatorApp()
                }
            }
        }
    }
}

@Composable
fun WalletNavigatorApp(viewModel: WalletViewModel = viewModel()) {
    val currentRoute by viewModel.currentRoute.collectAsState()

    Crossfade(
        targetState = currentRoute,
        label = "شاشات التطبيق"
    ) { route ->
        when (route) {
            "splash" -> SplashScreen(
                onTimeout = { viewModel.navigateTo("selection") }
            )

            "selection" -> SelectionScreen(
                viewModel = viewModel,
                onLoginClick = { viewModel.navigateTo("login") },
                onSignUpClick = { viewModel.navigateTo("signup") },
                onAdminSuccess = { viewModel.navigateTo("admin") }
            )

            "signup" -> SignUpScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateTo("selection") },
                onSuccess = { viewModel.navigateTo("home") }
            )

            "login" -> LoginScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateTo("selection") },
                onSuccessHome = { viewModel.navigateTo("home") },
                onSuccessKYC = { viewModel.navigateTo("kyc") },
                onSignUpClick = { viewModel.navigateTo("signup") }
            )

            "kyc" -> KYCScreen(
                viewModel = viewModel,
                onSuccess = { viewModel.navigateTo("home") }
            )

            "home" -> MainTabHost(
                viewModel = viewModel,
                onLogoutClick = { viewModel.logout() },
                onAdminClick = { viewModel.navigateTo("admin") }
            )

            "admin" -> AdminPanelScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateTo("home") }
            )
        }
    }
}
