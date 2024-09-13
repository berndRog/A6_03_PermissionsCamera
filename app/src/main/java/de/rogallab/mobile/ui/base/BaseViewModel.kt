package de.rogallab.mobile.ui.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logVerbose
import de.rogallab.mobile.ui.errors.ErrorParams
import de.rogallab.mobile.ui.errors.ErrorUiState
import de.rogallab.mobile.ui.navigation.NavEvent
import de.rogallab.mobile.ui.navigation.NavUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

open class BaseViewModel(
   application: Application,
   private val _tag: String
) : AndroidViewModel(application) {

   // Error  State = ViewModel (one time) events
   private val _errorUiStateFlow: MutableStateFlow<ErrorUiState> = MutableStateFlow(ErrorUiState())
   val errorUiStateFlow: StateFlow<ErrorUiState> = _errorUiStateFlow.asStateFlow()

   fun onErrorEvent(params: ErrorParams) {
      logDebug(_tag, "onErrorEvent()")
      _errorUiStateFlow.update { it: ErrorUiState ->
         it.copy(params = params)
      }
   }
   fun onErrorEventHandled() {
      logDebug(_tag, "onErrorEventHandled()")
      _errorUiStateFlow.update { it: ErrorUiState ->
         it.copy(params = null)
      }
   }

   fun onFailure(throwable: Throwable, navEvent: NavEvent? = null) {
      when (throwable) {
         is CancellationException -> {
            val error = throwable.localizedMessage ?: "Cancellation error"
            _errorUiStateFlow.value = _errorUiStateFlow.value.copy(
               params = ErrorParams(message = error, navEvent = navEvent)
            )
         }
         /*
         is RedirectResponseException -> {
            val error = "Redirect error: ${throwable.response.status.description}"
            onErrorEvent(ErrorParams(message = error, navEvent = navEvent))
         }
         is ClientRequestException -> {
            val error = "Client error: ${throwable.response.status.description}"
            onErrorEvent(ErrorParams(message = error, navEvent = navEvent))
         }
         is ServerResponseException -> {
            val error = "Server error: ${throwable.response.status.description}"
            onErrorEvent(ErrorParams(message = error, navEvent = navEvent))
         }
         is ConnectTimeoutException ->
            onErrorEvent(ErrorParams(message = "Connect timed out", navEvent = navEvent))
         is SocketTimeoutException ->
            onErrorEvent(ErrorParams(message = "Socket timed out", navEvent = navEvent))
         is UnknownHostException ->
            onErrorEvent(ErrorParams(message = "No internet connection", navEvent = navEvent))
         */
         else ->
            onErrorEvent(ErrorParams(throwable = throwable, navEvent = navEvent))
      }
   }


   // Navigation State = ViewModel (one time) UI event
   private val _navUiStateFlow: MutableStateFlow<NavUiState> = MutableStateFlow(NavUiState())
   val navUiStateFlow: StateFlow<NavUiState> = _navUiStateFlow.asStateFlow()

   // prevent multiple navigation events
   private var navEvent: NavEvent? = null

   fun navigateTo(event: NavEvent) {
      logVerbose(_tag, "navigateTo() event:${event.toString()}")
      if (event == navEvent) return
      navEvent = event
      _navUiStateFlow.update { it: NavUiState ->
         it.copy(event = event)
      }
   }
   fun onNavEventHandled() {
      logVerbose(_tag, "onNavEventHandled() event: null")
      viewModelScope.launch {
         delay(100) // Delay to ensure navigation has been processed
         _navUiStateFlow.update { it: NavUiState ->
            it.copy(event = null)
         }
         navEvent = null
      }
   }

}