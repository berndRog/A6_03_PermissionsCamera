package de.rogallab.mobile.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.rogallab.mobile.domain.utilities.logVerbose
import kotlinx.coroutines.flow.StateFlow

class NavigationViewModel() : ViewModel(),
//  KoinComponent,
    INavigationHandler {

// private val navigationHandler: NavigationHandler by inject { parametersOf(viewModelScope, _tag) }
   private val navigationHandler: NavigationHandler = NavigationHandler(viewModelScope)

   override val navUiStateFlow: StateFlow<NavUiState>
      get() = navigationHandler.navUiStateFlow

   override fun navigateTo(event: NavEvent) {
      logVerbose("<-NavigationViewModel", "navigateTo() event:${event.toString()}")
      navigationHandler.navigateTo(event)
   }

   override fun onNavEventHandled() {
      logVerbose("<-NavigationViewModel", "onNavEventHandled()")
      navigationHandler.onNavEventHandled()
   }
}