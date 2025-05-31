package com.example.votingappnew

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.votingappnew.ui.theme.VotingAppNewTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        auth = FirebaseAuth.getInstance()

        setContent {
            VotingAppNewTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    VotingAppNavigation(navController = navController, auth = auth)
                }
            }
        }
    }
}

@Composable
fun VotingAppNavigation(navController: NavHostController, auth: FirebaseAuth) {
    NavHost(
        navController = navController,
        startDestination = if (auth.currentUser == null) "login" else "main"
    ) {
        composable("login") {
            LoginScreen(
                navController = navController,
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("main") {
            MainScreen(navController = navController)
        }
        composable("candidate") {
            // شاشة المرشحين المؤقتة (تقدر تضيفها بعدين)
        }
    }
}
