package kurd.reco.mobile

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.DiscoverScreenDestination
import com.ramcosta.composedestinations.generated.destinations.FavoritesScreenDestination
import com.ramcosta.composedestinations.generated.destinations.HomeScreenRootDestination
import com.ramcosta.composedestinations.generated.destinations.SearchScreenDestination
import com.ramcosta.composedestinations.generated.destinations.SettingScreenRootDestination
import com.ramcosta.composedestinations.spec.DirectionDestinationSpec
import com.ramcosta.composedestinations.utils.currentDestinationAsState
import com.ramcosta.composedestinations.utils.startDestination
import com.ramcosta.composedestinations.utils.toDestinationsNavigator

enum class NavigationBarDestination(
    val direction: DirectionDestinationSpec,
    val icon: ImageVector,
    val label: String
) {
    Home(
        direction = HomeScreenRootDestination,
        icon = Icons.Default.Home,
        label = "Home"
    ),
    Discover(
        direction = DiscoverScreenDestination,
        icon = Icons.Outlined.Explore,
        label = "Discover"
    ),
    Search(
        direction = SearchScreenDestination,
        icon = Icons.Default.Search,
        label = "Search"
    ),
    Favorite(
        direction = FavoritesScreenDestination,
        icon = Icons.Default.FavoriteBorder,
        label = "Favorite"
    ),
//    Download(
//        direction = DownloadScreenDestination,
//        icon = Icons.Default.Done,
//        label = "Download"
//    ),
    Settings(
        direction = SettingScreenRootDestination,
        icon = Icons.Default.Settings,
        label = "Settings"
    )
}

@Composable
fun BottomBar(
    navController: NavController
) {
    val currentDestination = navController.currentDestinationAsState().value
        ?: NavGraphs.root.startDestination

    NavigationBar(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp)),
        tonalElevation = 10.dp,
        windowInsets = WindowInsets.navigationBars
    ) {
        NavigationBarDestination.entries.forEach { destination ->
            NavigationBarItem(
                selected = currentDestination == destination.direction,
                onClick = {
                    navController.toDestinationsNavigator().navigate(destination.direction) {
                        launchSingleTop = true
                    }
                },
                icon = { Icon(destination.icon, contentDescription = destination.label) },
                label = { Text(destination.label, style = MaterialTheme.typography.labelMedium) },
            )
        }
    }
}

@Composable
fun SideBar(
    navController: NavController
) {
    val currentDestination = navController.currentDestinationAsState().value
        ?: NavGraphs.root.startDestination

    Box(Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
        Column {
            NavigationBarDestination.entries.forEach { destination ->
                NavigationRailItem(
                    selected = currentDestination == destination.direction,
                    onClick = {
                        navController.toDestinationsNavigator().navigate(destination.direction) {
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(destination.icon, contentDescription = destination.label) },
                    label = {}
                )
            }
        }
    }
}