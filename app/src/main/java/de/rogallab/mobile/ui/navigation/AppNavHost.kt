package de.rogallab.mobile.ui.navigation
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.ui.home.HomeScreen
import de.rogallab.mobile.ui.people.composables.PeopleSwipeListScreen
import de.rogallab.mobile.ui.people.PeopleViewModel
import de.rogallab.mobile.ui.people.composables.PersonScreen
import de.rogallab.mobile.ui.sensors.location.LocationsViewModel
import de.rogallab.mobile.ui.sensors.environment_orientation.EnvOriSensorsViewModel
import de.rogallab.mobile.ui.sensors.location.composables.LocationsListScreen
import de.rogallab.mobile.ui.sensors.environment_orientation.composables.SensorsListScreen
import de.rogallab.mobile.ui.settings.SettingsScreen
import de.rogallab.mobile.ui.settings.SettingsViewModel

@Composable
fun AppNavHost(
   // Injecting the ViewModel by koin()
   peopleViewModel: PeopleViewModel = viewModel(),
   locationsViewModel: LocationsViewModel, // = viewModel(),
   envOriSensorsViewModel: EnvOriSensorsViewModel, // = viewModel()
   settingsViewModel: SettingsViewModel = viewModel(),
   drawerState: DrawerState
) {
   val tag = "<AppNavHost>"
   val duration = 1000  // in milliseconds
   // create a NavHostController with a factory function
   val navController: NavHostController = rememberNavController()

   NavHost(
      navController = navController,
      startDestination = NavScreen.Home.route,
      enterTransition = { enterTransition(duration) },
      exitTransition  = { exitTransition(duration)  },
      popEnterTransition = { popEnterTransition(duration) },
      popExitTransition = { popExitTransition(duration) }
   ) {

      composable( route = NavScreen.Home.route ) {
         HomeScreen(
            navController = navController
         )
      }
      composable( route = NavScreen.PeopleList.route ) {
         PeopleSwipeListScreen(
            viewModel = peopleViewModel,
            navController = navController
         )
      }

      composable( route = NavScreen.PersonInput.route ) {
         PersonScreen(
            viewModel = peopleViewModel,
            isInputScreen = true
         )
      }

      composable(
         route = NavScreen.PersonDetail.route + "/{personId}",
         arguments = listOf(navArgument("personId") { type = NavType.StringType}),
      ) { backStackEntry ->
         val id = backStackEntry.arguments?.getString("personId")
         PersonScreen(
            viewModel = peopleViewModel,
            isInputScreen = false,
            id = id
         )
      }

      composable( route = NavScreen.LocationsList.route )  {
         LocationsListScreen(
            viewModel = locationsViewModel,
            navController = navController
         )
      }

      composable( route = NavScreen.SensorsList.route ) {
         SensorsListScreen(
            viewModel = envOriSensorsViewModel,
            navController = navController
         )
      }

      composable( route = NavScreen.Settings.route ) {
         SettingsScreen(
            settingsViewModel = settingsViewModel
         )
      }

   }

   // Observing the navigation state and handle navigation
   val navUiState: NavUiState by peopleViewModel.navUiStateFlow.collectAsStateWithLifecycle()
   navUiState.event?.let { navEvent: NavEvent ->
      logInfo(tag, "navEvent: $navEvent")
      when(navEvent) {

         is NavEvent.Home -> {
            navController.navigate(NavScreen.Home.route) {
               popUpTo(navController.graph.startDestinationRoute ?: NavScreen.Home.route) {
                  saveState = true
               }
               launchSingleTop = true
               restoreState = true
            }
            peopleViewModel.onNavEventHandled()
         }

         is NavEvent.NavigateForward -> {
            // Each navigate() pushes the given destination
            // to the top of the stack.
            navController.navigate(navEvent.route)

            // onNavEventHandled() resets the navEvent to null
            peopleViewModel.onNavEventHandled()
         }

         is NavEvent.NavigateReverse -> {
            navController.navigate(navEvent.route) {
               popUpTo(navEvent.route) {  // clears the back stack up to the given route
                  inclusive = true        // ensures that any previous instances of
               }                          // that route are removed
            }

            // onNavEventHandled() resets the navEvent to null
            peopleViewModel.onNavEventHandled()
         }

         is NavEvent.NavigateBack -> {
            navController.popBackStack()

            // onNavEventHandled() resets the navEvent to null
            peopleViewModel.onNavEventHandled()
         }

         is NavEvent.BottomNav -> {
            // navigateUp() pops the back stack to the previous destination
            navController.popBackStack()
            navController.navigate(navEvent.route) {
               navController.graph.startDestinationRoute?.let { route ->
                  popUpTo(route) { saveState = true  }
               }
               // Avoid multiple copies of the same destination when
               // reselecting the same item
               launchSingleTop = true
               // Restore state when reselecting a previously selected item
               restoreState = true
            }

            peopleViewModel.onNavEventHandled()
         }

      }
   }

}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition(
   duration: Int
) = fadeIn(
   animationSpec = tween(duration)
) + slideIntoContainer(
   animationSpec = tween(duration),
   towards = AnimatedContentTransitionScope.SlideDirection.Right
)

private fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransition(
   duration: Int
) = fadeOut(
   animationSpec = tween(duration)
) + slideOutOfContainer(
   animationSpec = tween(duration),
   towards = AnimatedContentTransitionScope.SlideDirection.Right
)


private fun AnimatedContentTransitionScope<NavBackStackEntry>.popEnterTransition(
   duration: Int
) = scaleIn(
   initialScale = 0.1f,
   animationSpec = tween(duration)
) + fadeIn(animationSpec = tween(duration))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.popExitTransition(
   duration: Int
) = scaleOut(
   targetScale = 3.0f,
   animationSpec = tween(duration)
) + fadeOut(animationSpec = tween(duration))
