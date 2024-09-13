package de.rogallab.mobile.ui.errors

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import de.rogallab.mobile.ui.navigation.NavEvent

suspend fun showError(
   snackbarHostState: SnackbarHostState,                   // State ↓
   params: ErrorParams,                                    // State ↓
   navigateTo: (NavEvent) -> Unit,                         // Event ↑                                // State ↓
) {

   snackbarHostState.showSnackbar(
      message = params.throwable?.message ?: params.message,
      actionLabel = params.actionLabel,
      withDismissAction = params.withDismissAction,
      duration = params.duration
   ).also { snackbarResult: SnackbarResult ->
      if (snackbarResult == SnackbarResult.ActionPerformed) {
         params.onDismissAction()
      }
   }

   // if navigation is true, navigate to route
   params.navEvent?.let { event ->
      navigateTo(event)
   }
}