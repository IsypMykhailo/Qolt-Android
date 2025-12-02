package ca.qolt.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import ca.qolt.ui.navigation.Destination

enum class TabItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String,
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home, Destination.Main.Home.route),
    BLOCKS("Blocks", Icons.Filled.Lock, Icons.Outlined.Lock, Destination.Main.Blocks.route),
    STATS("Stats", Icons.Filled.BarChart, Icons.Outlined.BarChart, Destination.Main.Statistics.route),
    PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.Person, Destination.Main.Profile.route)
}

@Composable
fun HomeBottomBar(navController: NavController) {
    val tabs = remember { TabItem.entries.toTypedArray().asList() }
    val routes = remember { TabItem.entries.map{it.route} }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Destination.Main.Home.route

    HomeBottomBarView(
        tabs = tabs,
        routes = routes,
        currentRoute = currentRoute,
        tabClick = {
            if(it.route != currentRoute) {
                navController.navigate(it.route) {
                    popUpTo(navController.graph.startDestinationId) {saveState = true}
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    )
}

@Composable
private fun HomeBottomBarView(
    tabs: List<TabItem>,
    routes: List<String>,
    currentRoute: String,
    tabClick: (TabItem) -> Unit
) {
    if(currentRoute in routes) {
        NavigationBar(
            tonalElevation = 4.dp,
            containerColor = Color(0xFF2C2C2E)
        ) {
            tabs.forEach { tab ->
                val selected = currentRoute == tab.route
                val icon = if (selected) tab.selectedIcon else tab.unselectedIcon
                val contentColor = if (selected) Color(0xFFFF6A1A) else Color(0xFF888888)
                val backgroundColor = if (selected) Color(0xFFFF6A1A).copy(alpha = 0.15f) else Color.Transparent

                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = tab.title,
                            tint = contentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    selected = selected,
                    onClick = {tabClick(tab)},
//                    label = {
//                        if (selected) {
//                            Text(
//                                text = tab.title,
//                                color = contentColor,
//                                fontSize = 12.sp,
//                                fontWeight = FontWeight.Medium
//                            )
//                        }
//                    },
                    alwaysShowLabel = false,
//                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).background(backgroundColor, RoundedCornerShape(12.dp)),
                )
            }
        }
    }
}
