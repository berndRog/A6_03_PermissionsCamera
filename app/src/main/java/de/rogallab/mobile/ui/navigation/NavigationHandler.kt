package de.rogallab.mobile.ui.navigation

import de.rogallab.mobile.domain.utilities.logVerbose
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NavigationHandler(
   private val viewModelScope: CoroutineScope
) : INavigationHandler {

   private val _navUiStateFlow: MutableStateFlow<NavUiState> =
      MutableStateFlow(NavUiState())
   override val navUiStateFlow: StateFlow<NavUiState> =
      _navUiStateFlow.asStateFlow()

   private var navEvent: NavEvent? = null

   override fun navigateTo(event: NavEvent) {
      if (event == navEvent) return
      navEvent = event
      _navUiStateFlow.update { it: NavUiState ->
         it.copy(event = event)
      }
   }

   override fun onNavEventHandled() {
       viewModelScope.launch {
         delay(100) // Delay to ensure navigation has been processed
         _navUiStateFlow.update { it: NavUiState ->
            it.copy(event = null)
         }
         navEvent = null
      }
   }
}