package de.rogallab.mobile.ui.navigation

sealed interface NavEvent {
   data class NavigateTo(val route: String) : NavEvent
   data object Back : NavEvent
}
