package de.rogallab.mobile.ui.navigation.composables
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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.ui.home.HomeScreen
import de.rogallab.mobile.ui.home.HomeViewModel
import de.rogallab.mobile.ui.navigation.INavigationHandler
import de.rogallab.mobile.ui.navigation.NavEvent
import de.rogallab.mobile.ui.navigation.NavScreen
import de.rogallab.mobile.ui.navigation.NavigationViewModel
import de.rogallab.mobile.ui.people.PeopleViewModel
import de.rogallab.mobile.ui.people.composables.PeopleSwipeListScreen
import de.rogallab.mobile.ui.people.composables.PersonScreen
import de.rogallab.mobile.ui.sensors.environment_orientation.SensorsViewModel
import de.rogallab.mobile.ui.sensors.environment_orientation.composables.SensorsListScreen
import de.rogallab.mobile.ui.sensors.location.LocationsViewModel
import de.rogallab.mobile.ui.sensors.location.composables.LocationsListScreen
import de.rogallab.mobile.ui.settings.SettingsScreen
import de.rogallab.mobile.ui.settings.SettingsViewModel
import kotlinx.coroutines.flow.combine

@Composable
fun AppNavHost(
   navController: NavHostController = rememberNavController(),
   // Injecting the ViewModel by koin()
   homeViewModel: HomeViewModel = viewModel(),
   peopleViewModel: PeopleViewModel, //,
   locationsViewModel: LocationsViewModel, // = viewModel(),
   sensorsViewModel: SensorsViewModel, // = viewModel()
   settingsViewModel: SettingsViewModel, // = viewModel(),
   navigationViewModel: NavigationViewModel, // = viewModel(),
) {
   val tag = "<AppNavHost>"
   val duration = 1000  // in milliseconds
   // create a NavHostController with a factory function

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
            viewModel = homeViewModel,
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
            viewModel = sensorsViewModel,
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
//   val navUiState1: NavUiState by peopleViewModel.navUiStateFlow.collectAsStateWithLifecycle()
//   val navUiState2: NavUiState by locationsViewModel.navUiStateFlow.collectAsStateWithLifecycle()
//   val navUiState3: NavUiState by peopleViewModel.navUiStateFlow.collectAsStateWithLifecycle()

   // Combine navUiStateFlow from multiple ViewModels
   val combinedNavEvent: NavEvent? by combine(
      homeViewModel.navUiStateFlow,
      peopleViewModel.navUiStateFlow,
      locationsViewModel.navUiStateFlow,
      sensorsViewModel.navUiStateFlow,
      navigationViewModel.navUiStateFlow
   ) { homeNavUiState, peopleNavUiState, locationsNavUiState, sensorsNavUiState, navigationNavUiState ->
      // Combine the states as needed, here we just return the first non-null event
      homeNavUiState.event ?:
      peopleNavUiState.event ?:
      locationsNavUiState.event ?:
      sensorsNavUiState.event ?:
      navigationNavUiState.event
   }.collectAsStateWithLifecycle(initialValue = null)

   combinedNavEvent?.let { navEvent: NavEvent ->
      logInfo(tag, "navEvent: $navEvent")
      // check which ViewModel has the navEvent
      val navigationHandler: INavigationHandler = when {
         homeViewModel.navUiStateFlow.value.event == navEvent -> homeViewModel
         peopleViewModel.navUiStateFlow.value.event == navEvent -> peopleViewModel
         locationsViewModel.navUiStateFlow.value.event == navEvent -> locationsViewModel
         sensorsViewModel.navUiStateFlow.value.event == navEvent -> sensorsViewModel
         navigationViewModel.navUiStateFlow.value.event == navEvent -> navigationViewModel
         else -> return@let
      }

      when(navEvent) {
         is NavEvent.NavigateHome -> {
            navController.navigate(NavScreen.Home.route) {
               popUpTo(navController.graph.startDestinationRoute ?: NavScreen.Home.route) {
                  saveState = true
               }
               launchSingleTop = true
               restoreState = true
            }
            navigationHandler.onNavEventHandled()
         }

         is NavEvent.NavigateLateral -> {
            navController.navigate(navEvent.route) {
               popUpTo(navController.graph.findStartDestination().id) {
                  saveState = true
               }
               launchSingleTop = true
               restoreState = true
            }
            navigationHandler.onNavEventHandled()
         }

         is NavEvent.NavigateForward -> {
            // Each navigate() pushes the given destination
            // to the top of the stack.
            navController.navigate(navEvent.route)

            // onNavEventHandled() resets the navEvent to null
            navigationHandler.onNavEventHandled()
         }

         is NavEvent.NavigateReverse -> {
            navController.navigate(navEvent.route) {
               popUpTo(navEvent.route) {  // clears the back stack up to the given route
                  inclusive = true        // ensures that any previous instances of
               }                          // that route are removed
            }
            navigationHandler.onNavEventHandled()
         }

         is NavEvent.NavigateBack -> {
            navController.popBackStack()
            navigationHandler.onNavEventHandled()
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
            navigationHandler.onNavEventHandled()
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
