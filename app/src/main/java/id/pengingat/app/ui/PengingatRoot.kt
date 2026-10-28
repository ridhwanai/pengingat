package id.pengingat.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import id.pengingat.app.MainActivity
import id.pengingat.app.ui.theme.Gold
import id.pengingat.app.ui.theme.NavySurface

private enum class Tab(val label: String, val icon: ImageVector) {
    Home("Beranda", Icons.Filled.Home),
    Calendar("Kalender", Icons.Filled.CalendarMonth),
    Journal("Jurnal", Icons.Filled.EditNote),
    Library("Refleksi", Icons.Filled.Book),
    Settings("Pengaturan", Icons.Filled.Settings)
}

@Composable
fun PengingatRoot(startScreen: String? = null) {
    val vm: AppViewModel = viewModel()
    val settings by vm.settings.collectAsState()

    LaunchedEffect(Unit) {
        vm.applyDueChanges()
        vm.refreshSystemStatus()
    }

    if (!settings.onboarded) {
        OnboardingScreen(vm = vm, onDone = { vm.finishOnboarding() })
        return
    }

    var tab by remember { mutableStateOf(Tab.Home) }
    var showCheckin by remember {
        mutableStateOf(startScreen == MainActivity.SCREEN_CHECKIN)
    }

    if (showCheckin) {
        CheckinScreen(vm = vm, onDone = { showCheckin = false })
        return
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = NavySurface) {
                Tab.values().forEach { t ->
                    NavigationBarItem(
                        selected = tab == t,
                        onClick = { tab = t },
                        icon = { Icon(t.icon, contentDescription = t.label) },
                        label = { Text(t.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Gold,
                            selectedTextColor = Gold,
                            indicatorColor = NavySurface
                        )
                    )
                }
            }
        }
    ) { padding ->
        androidx.compose.animation.AnimatedContent(
            targetState = tab,
            transitionSpec = {
                androidx.compose.animation.fadeIn(
                    animationSpec = androidx.compose.animation.core.tween(260)
                ) togetherWith androidx.compose.animation.fadeOut(
                    animationSpec = androidx.compose.animation.core.tween(180)
                )
            },
            label = "tabTransition",
            modifier = Modifier.padding(padding)
        ) { current ->
            when (current) {
                Tab.Home -> HomeScreen(
                    vm = vm,
                    onOpenCheckin = { showCheckin = true },
                    onOpenLibrary = { tab = Tab.Library }
                )
                Tab.Calendar -> CalendarScreen(vm = vm)
                Tab.Journal -> JournalScreen(vm = vm)
                Tab.Library -> LibraryScreen(vm = vm)
                Tab.Settings -> SettingsScreen(vm = vm)
            }
        }
    }
}
