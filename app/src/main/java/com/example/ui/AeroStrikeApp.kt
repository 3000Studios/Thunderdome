package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.GameViewModel

enum class MainNavTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    HANGAR("Hangar", Icons.Default.Flight),
    MISSIONS("Missions", Icons.Default.Public),
    BATTLE_PASS("Pass", Icons.Default.MilitaryTech),
    LEADERBOARD("Ranks", Icons.Default.Leaderboard),
    SETTINGS("Systems", Icons.Default.Settings)
}

@Composable
fun AeroStrikeApp(viewModel: GameViewModel) {
    var inCombatMode by remember { mutableStateOf(false) }
    var currentTab by remember { mutableStateOf(MainNavTab.HANGAR) }

    if (inCombatMode) {
        CombatScreen(
            viewModel = viewModel,
            onExitMission = { inCombatMode = false }
        )
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = DarkSurface,
                    contentColor = Color.White,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("main_bottom_nav")
                ) {
                    MainNavTab.values().forEach { tab ->
                        val selected = currentTab == tab
                        NavigationBarItem(
                            selected = selected,
                            onClick = { currentTab = tab },
                            icon = { Icon(tab.icon, contentDescription = tab.title) },
                            label = { Text(tab.title) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = DarkVoid,
                                selectedTextColor = AeroCyan,
                                indicatorColor = AeroCyan,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            },
            containerColor = DarkVoid
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentTab) {
                    MainNavTab.HANGAR -> HangarScreen(
                        viewModel = viewModel,
                        onLaunchMission = { inCombatMode = true }
                    )
                    MainNavTab.MISSIONS -> MissionSelectScreen(
                        viewModel = viewModel,
                        onLaunchMission = { inCombatMode = true }
                    )
                    MainNavTab.BATTLE_PASS -> BattlePassScreen(viewModel = viewModel)
                    MainNavTab.LEADERBOARD -> LeaderboardScreen(viewModel = viewModel)
                    MainNavTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
