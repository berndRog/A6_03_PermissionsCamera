package de.rogallab.mobile.ui.navigation.composables
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.ui.INavigationHandler
import de.rogallab.mobile.ui.camera.CameraScreen
import de.rogallab.mobile.ui.camera.CameraViewModel
import de.rogallab.mobile.ui.home.HomeScreen
import de.rogallab.mobile.ui.home.HomeViewModel
import de.rogallab.mobile.ui.navigation.NavEvent
import de.rogallab.mobile.ui.navigation.NavScreen
import de.rogallab.mobile.ui.navigation.NavState
import de.rogallab.mobile.ui.navigation.NavigationViewModel
import de.rogallab.mobile.ui.people.PeopleViewModel
import de.rogallab.mobile.ui.people.PersonValidator
import de.rogallab.mobile.ui.people.composables.PeopleListScreen
import de.rogallab.mobile.ui.people.composables.PersonScreen
import de.rogallab.mobile.ui.sensors.orientation.SensorsViewModel
import de.rogallab.mobile.ui.sensors.orientation.composables.SensorsListScreen
import de.rogallab.mobile.ui.sensors.location.LocationsViewModel
import de.rogallab.mobile.ui.sensors.location.composables.LocationsListScreen
import de.rogallab.mobile.ui.settings.SettingsScreen
import de.rogallab.mobile.ui.settings.SettingsViewModel
import kotlinx.coroutines.flow.combine
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import javax.xml.validation.Validator

@Composable
fun AppNavHost(
   navController: NavHostController = koinInject<NavHostController>(),
   homeViewModel: HomeViewModel = koinViewModel<HomeViewModel>(),
   peopleViewModel: PeopleViewModel = koinViewModel<PeopleViewModel>(),
   cameraViewModel: CameraViewModel = koinViewModel<CameraViewModel>(),
   locationsViewModel: LocationsViewModel = koinViewModel<LocationsViewModel>(),
   sensorsViewModel: SensorsViewModel = koinViewModel<SensorsViewModel>(),
   settingsViewModel: SettingsViewModel = koinViewModel<SettingsViewModel>(),
   navigationViewModel: NavigationViewModel, // = viewModel(),

   personInputValidator: PersonValidator = koinInject<PersonValidator>()
) {
   val tag = "<-AppNavHost"
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
      // H O M E ---------------------------------------------------------------
      composable( route = NavScreen.Home.route ) {
         HomeScreen(
            viewModel = homeViewModel,
            navController = navController
         )
      }
      // P E O P L E ---------------------------------------------------------
      composable( route = NavScreen.PeopleList.route ) {
         PeopleListScreen(
            viewModel = peopleViewModel,
            navController = navController
         )
      }
      composable( route = NavScreen.PersonInput.route ) {
         PersonScreen(
            viewModel = peopleViewModel,
            validator = personInputValidator,
            isInputScreen = true,
         )
      }
      composable(
         route = NavScreen.PersonDetail.route + "/{personId}",
         arguments = listOf(navArgument("personId") { type = NavType.StringType}),
      ) { backStackEntry ->
         val id = backStackEntry.arguments?.getString("personId")
         PersonScreen(
            viewModel = peopleViewModel,
            validator = personInputValidator,
            isInputScreen = false,
            id = id
         )
      }
      // C A M E R A (Photo&Video) --------------------------------------------
      composable( route = NavScreen.Camera.route )  {
         CameraScreen(
            viewModel = cameraViewModel
         )
      }
      // L O C A T I O N S ----------------------------------------------------
      composable( route = NavScreen.LocationsList.route )  {
         LocationsListScreen(
            viewModel = locationsViewModel,
            navController = navController
         )
      }
      // S E N S O R S ---------------------------------------------------------
      composable( route = NavScreen.SensorsList.route ) {
         SensorsListScreen(
            viewModel = sensorsViewModel,
            navController = navController
         )
      }
      // S E T T I N G S --------------------------------------------------------
      composable( route = NavScreen.Settings.route ) {
         SettingsScreen(
            settingsViewModel = settingsViewModel
         )
      }

   }

   // Observing the navigation state and handle navigation
//   val navUiState1: NavState by peopleViewModel.navStateFlow.collectAsStateWithLifecycle()
//   val navUiState2: NavState by locationsViewModel.navStateFlow.collectAsStateWithLifecycle()

   // Combine navStateFlow from multiple ViewModels
   val combinedNavEvent: NavEvent? by combine(
      homeViewModel.navStateFlow,
      cameraViewModel.navStateFlow,
      peopleViewModel.navStateFlow,
      locationsViewModel.navStateFlow,
      sensorsViewModel.navStateFlow,
      settingsViewModel.navStateFlow,
      navigationViewModel.navStateFlow
   ) { navStates: Array<NavState> ->
      // Combine the states as needed, here we just return the first non-null event
      navStates.mapNotNull { it.navEvent }.firstOrNull()
   }.collectAsStateWithLifecycle(initialValue = null)

   logInfo(tag, "combinedNavEvent: $combinedNavEvent")

   combinedNavEvent?.let { navEvent: NavEvent ->
      logInfo(tag, "navEvent: $navEvent")
      // check which ViewModel has the navEvent
      val navigationHandler: INavigationHandler = when {
         homeViewModel.navStateFlow.value.navEvent == navEvent -> homeViewModel
         cameraViewModel.navStateFlow.value.navEvent == navEvent -> cameraViewModel
         peopleViewModel.navStateFlow.value.navEvent == navEvent -> peopleViewModel
         locationsViewModel.navStateFlow.value.navEvent == navEvent -> locationsViewModel
         sensorsViewModel.navStateFlow.value.navEvent == navEvent -> sensorsViewModel
         settingsViewModel.navStateFlow.value.navEvent == navEvent -> settingsViewModel
         navigationViewModel.navStateFlow.value.navEvent == navEvent -> navigationViewModel
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
   targetScale = 2.0f,
   animationSpec = tween(duration)
) + fadeOut(animationSpec = tween(duration))
