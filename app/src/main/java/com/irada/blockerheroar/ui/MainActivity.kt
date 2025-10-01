package com.irada.blockerheroar.ui

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.irada.blockerheroar.IradaApplication
import com.irada.blockerheroar.R
import com.irada.blockerheroar.ui.screens.HomeScreen
import com.irada.blockerheroar.ui.screens.BlockedListScreen
import com.irada.blockerheroar.ui.screens.SettingsScreen
import com.irada.blockerheroar.ui.theme.IradaTheme
import com.irada.blockerheroar.utils.AppPreferences
import com.irada.blockerheroar.utils.PartnerManager
import kotlinx.coroutines.launch
import android.widget.Toast

class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            IradaTheme {
                MainScreen()
            }
        }
    }
    
    override fun attachBaseContext(newBase: Context?) {
        val context = newBase?.let { IradaApplication.updateLanguage(it) } ?: newBase
        super.attachBaseContext(context)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // التحقق من تحديثات الشراكة عند فتح التطبيق
    LaunchedEffect(Unit) {
        scope.launch {
            val message = PartnerManager.checkPartnerUpdates()
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
        }
    }
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                listOf(
                    NavigationItem.Home,
                    NavigationItem.BlockedList,
                    NavigationItem.Settings
                ).forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = stringResource(item.titleRes)) },
                        label = { Text(stringResource(item.titleRes)) }, // Use stringResource
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavigationItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavigationItem.Home.route) {
                HomeScreen()
            }
            composable(NavigationItem.BlockedList.route) {
                BlockedListScreen()
            }
            composable(NavigationItem.Settings.route) {
                SettingsScreen()
            }
        }
    }
}

sealed class NavigationItem(
    val route: String,
    val titleRes: Int, // Changed to resource ID
    val icon: ImageVector
) {
    object Home : NavigationItem("home", R.string.nav_home, Icons.Default.Home)
    object BlockedList : NavigationItem("blocked_list", R.string.nav_blocked_list, Icons.Default.Block)
    object Settings : NavigationItem("settings", R.string.nav_settings, Icons.Default.Settings)
}
