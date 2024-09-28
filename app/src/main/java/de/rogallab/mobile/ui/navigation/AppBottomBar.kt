package de.rogallab.mobile.ui.navigation

import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import de.rogallab.mobile.domain.utilities.logDebug

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBottomBar(
   navController: NavController
) {
   val topLevelScreens = listOf(
      NavScreen.Home,
      NavScreen.PeopleList,
      NavScreen.LocationsList,
      NavScreen.SensorsList
   )

   NavigationBar(
   //   containerColor = MaterialTheme.colorScheme.primary,
   //   contentColor = MaterialTheme.colorScheme.onPrimary
   ) {
      val tag = "<-AppBottomBar"

      val navBackStackEntry by navController.currentBackStackEntryAsState()
      val currentRoute = navBackStackEntry?.destination?.route

      topLevelScreens.forEach { topLevelScreen ->
         NavigationBarItem(
            icon = {
               BadgedBox(
                  badge = {
                     if(topLevelScreen.badgeCount != null) {
                        Badge { Text(text = topLevelScreen.badgeCount.toString() ) }
                     } else if(topLevelScreen.hasNews) {
                        Badge()
                     }
                  }
               ) {
                  Icon(
                     imageVector =
                        if(currentRoute == topLevelScreen.route) topLevelScreen.selectedIcon
                        else                           topLevelScreen.unSelectedIcon,
                     contentDescription = topLevelScreen.title
                  )
               }
            },
            label = { Text(text = topLevelScreen.title) },
            alwaysShowLabel = true,
            selected = currentRoute == topLevelScreen.route,
            onClick = {
               logDebug(tag,"navigateTo ${topLevelScreen.route}")
               navController.navigate(topLevelScreen.route) {
                  // Pop up to the start destination of the graph to
                  // avoid building up a large stack of destinations
                  // on the back stack as users select items
                  popUpTo(navController.graph.findStartDestination().id) {
                     saveState = true
                  }
                  // Avoid multiple copies of the same destination when
                  // reselecting the same item
                  launchSingleTop = true
                  // Restore state when reselecting a previously selected item
                  restoreState = true
               }
            }
         )
      }
   }
}