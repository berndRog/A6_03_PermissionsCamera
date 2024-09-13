package de.rogallab.mobile.ui.navigation
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import de.rogallab.mobile.ui.people.composables.PeopleSwipeListScreen
import de.rogallab.mobile.ui.people.PeopleViewModel
import de.rogallab.mobile.ui.people.composables.PersonScreen

@Composable
fun AppNavHost(
   // create a NavHostController with a factory function
   navController: NavHostController = rememberNavController(),
   // Injecting the ViewModel by koin()
   peopleViewModel: PeopleViewModel = viewModel()
) {
   val tag = "[AppNavHost]"
   val duration = 1000  // in milliseconds

   NavHost(
      navController = navController,
      startDestination = NavScreen.PeopleList.route,
      enterTransition = { enterTransition(duration) },
      exitTransition  = { exitTransition(duration)  },
      popEnterTransition = { popEnterTransition(duration) },
      popExitTransition = { popExitTransition(duration) }
   ) {
      composable( route = NavScreen.PeopleList.route ) {
         PeopleSwipeListScreen(
            viewModel = peopleViewModel
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
   }

   // Observing the navigation state and handle navigation
   val navUiState: NavUiState by peopleViewModel.navUiStateFlow.collectAsStateWithLifecycle()
   navUiState.event?.let { navEvent: NavEvent ->
      logInfo(tag, "navEvent: $navEvent")
      when(navEvent) {

         is NavEvent.NavigateTo -> {
            // Each navigate() pushes the given destination
            // to the top of the stack.
            navController.navigate(navEvent.route) {
               // popUpTo() clears the back stack up to the given route
               // inclusive = true -> ensures that any previous instances of
               // that route are removed
//               popUpTo(navEvent.route) {
//                  inclusive = false
//               }
            }
            // onNavEventHandled() resets the navEvent to null
            peopleViewModel.onNavEventHandled()
         }

         is NavEvent.Back -> {
            // navigateUp() pops the back stack to the previous destination
            navController.popBackStack()

            peopleViewModel.onNavEventHandled()
         }
      }
   }

}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition(
   duration: Int
) = fadeIn(animationSpec = tween(duration)) + slideIntoContainer(
      animationSpec = tween(duration),
      towards = AnimatedContentTransitionScope.SlideDirection.Left
   )

private fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransition(
   duration: Int
) = fadeOut(animationSpec = tween(duration)) + slideOutOfContainer(
         animationSpec = tween(duration),
         towards = AnimatedContentTransitionScope.SlideDirection.Left
      )

private fun AnimatedContentTransitionScope<NavBackStackEntry>.popEnterTransition(
   duration: Int
) = fadeIn(animationSpec = tween(duration)) + slideIntoContainer(
   animationSpec = tween(duration),
   towards = AnimatedContentTransitionScope.SlideDirection.Up
)

private fun AnimatedContentTransitionScope<NavBackStackEntry>.popExitTransition(
   duration: Int
) = fadeOut(animationSpec = tween(duration)) + slideOutOfContainer(
   animationSpec = tween(duration),
   towards = AnimatedContentTransitionScope.SlideDirection.Up
)
