package kurd.reco.roxio.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.tv.material3.DrawerValue
import androidx.tv.material3.Icon
import androidx.tv.material3.NavigationDrawer
import androidx.tv.material3.NavigationDrawerItem
import androidx.tv.material3.Text
import androidx.tv.material3.rememberDrawerState
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.DiscoverScreenDestination
import com.ramcosta.composedestinations.generated.destinations.FavoritesScreenDestination
import com.ramcosta.composedestinations.generated.destinations.HomeScreenDestination
import com.ramcosta.composedestinations.generated.destinations.SearchScreenDestination
import com.ramcosta.composedestinations.generated.destinations.SettingsScreenDestination
import com.ramcosta.composedestinations.spec.DirectionDestinationSpec
import com.ramcosta.composedestinations.utils.currentDestinationAsState
import com.ramcosta.composedestinations.utils.startDestination
import com.ramcosta.composedestinations.utils.toDestinationsNavigator
import kurd.reco.roxio.R

enum class NavigationBarDestination(
    val direction: DirectionDestinationSpec,
    val icon: ImageVector? = null,
    @DrawableRes val resourceId: Int? = null,
    val label: String
) {
    Home(
        direction = HomeScreenDestination,
        icon = Icons.Default.Home,
        label = "Home"
    ),
    Discover(
        direction = DiscoverScreenDestination,
        resourceId = R.drawable.outline_explore_24,
        label = "Discover"
    ),
    Search(
        direction = SearchScreenDestination,
        icon = Icons.Default.Search,
        label = "Search"
    ),
    Favorites(
        direction = FavoritesScreenDestination,
        icon = Icons.Default.FavoriteBorder,
        label = "Favorites"
    ),
    Settings(
        direction = SettingsScreenDestination,
        icon = Icons.Default.Settings,
        label = "Settings"
    )
}

@Composable
fun RoxioNavigationDrawer(navController: NavController, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val currentDestination = navController.currentDestinationAsState().value
        ?: NavGraphs.root.startDestination

    NavigationDrawer(
        drawerState = drawerState,
        drawerContent = { drawerValue ->
            val isClosed = drawerValue == DrawerValue.Closed
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(10.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                NavigationBarDestination.entries.forEach {
                    NavigationDrawerItem(
                        selected = isClosed && currentDestination == it.direction,
                        onClick = {
                            navController.toDestinationsNavigator().navigate(it.direction) {
                                launchSingleTop = true
                            }
                        },
                        leadingContent = { Icon(it.icon ?: ImageVector.vectorResource(it.resourceId!!), contentDescription = it.label) }
                    ) {
                        Text(text = it.label)
                    }
                }
            }
        }
    ) {
        content()
    }
}