package de.rogallab.mobile.ui.errors

import androidx.compose.material3.SnackbarDuration
import de.rogallab.mobile.ui.navigation.NavEvent

data class ErrorParams(
   val throwable: Throwable? = null,
   val message: String = "",

   // Snackbar parameters
   // no actionLabel means no action
   val actionLabel: String? = null,
   // duration of the snackbars visibility
   val duration: SnackbarDuration = SnackbarDuration.Short,
   // dismiss action
   val withDismissAction: Boolean = false,
   val onDismissAction: () -> Unit = {}, // default action: do nothing

   // navigation to
   var navEvent: NavEvent?  = null,
)