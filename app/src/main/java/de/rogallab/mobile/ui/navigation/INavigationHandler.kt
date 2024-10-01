package de.rogallab.mobile.ui.navigation

import kotlinx.coroutines.flow.StateFlow

interface INavigationHandler {
   val navUiStateFlow: StateFlow<NavUiState>
   fun navigateTo(event: NavEvent)
   fun onNavEventHandled()
}