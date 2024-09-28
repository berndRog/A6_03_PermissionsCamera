package de.rogallab.mobile.ui.people.composables

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.safeGestures
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.rogallab.mobile.R
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.ui.composables.InputEmail
import de.rogallab.mobile.ui.composables.InputName
import de.rogallab.mobile.ui.composables.InputPhone
import de.rogallab.mobile.ui.composables.SelectAndShowImage
import de.rogallab.mobile.ui.errors.ErrorParams
import de.rogallab.mobile.ui.errors.ErrorUiState
import de.rogallab.mobile.ui.errors.showError
import de.rogallab.mobile.ui.navigation.NavEvent
import de.rogallab.mobile.ui.navigation.NavScreen
import de.rogallab.mobile.ui.people.PeopleViewModel
import de.rogallab.mobile.ui.people.PersonUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonScreen(
   viewModel: PeopleViewModel,
   isInputScreen: Boolean,
   id: String? = null
) {
   val isInputMode: Boolean by rememberSaveable { mutableStateOf(isInputScreen) }

   // Observe the state of the viewmodel
   val personUiState: PersonUiState
      by viewModel.personUiStateFlow.collectAsStateWithLifecycle()

   val screenTitle =
      if (isInputMode) stringResource(R.string.person_input)
      else stringResource(R.string.person_detail)
   val tag =
      if (isInputMode) "[PersonInputScreen]"
      else "[PersonDetailScreen]"
   // DetailScreen
   if (!isInputMode) {
      id?.let { it: String ->
         LaunchedEffect(Unit) {
            viewModel.fetchPerson(it)
         }
      } ?: run {
         viewModel.onErrorEvent(
            ErrorParams(
               message = "No id for person is given",
               navEvent = NavEvent.NavigateBack(NavScreen.PeopleList.route)
            )
         )
      }
   }

   BackHandler{
      logInfo(tag, "BackHandler -> navigate to Peoplelist")
      viewModel.navigateTo(NavEvent.NavigateBack(NavScreen.PeopleList.route))
   }

   val windowInsets = WindowInsets.systemBars
      .add(WindowInsets.captionBar)
      .add(WindowInsets.ime)
      .add(WindowInsets.safeGestures)

   val snackbarHostState = remember { SnackbarHostState() }

   Scaffold(
      modifier = Modifier
         .fillMaxSize()
         .padding(windowInsets.asPaddingValues())
         .background(color = MaterialTheme.colorScheme.surface),
      topBar = {
         TopAppBar(
            title = { Text(text = screenTitle) },
            navigationIcon = {
               IconButton(onClick = {
                  logDebug(tag, "Reverse navigation")
                  viewModel.validate(isInputMode)
                  viewModel.navigateTo(NavEvent.NavigateReverse(NavScreen.PeopleList.route))
               }) {
                  Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                     contentDescription = stringResource(R.string.back))
               }
            }
         )
      },
      snackbarHost = {
         SnackbarHost(hostState = snackbarHostState) { data ->
            Snackbar(
               snackbarData = data,
               actionOnNewLine = true
            )
         }
      }) { paddingValues: PaddingValues ->
      Column(
         modifier = Modifier
            .padding(paddingValues)
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .imePadding() // padding for the bottom for the IME
      ) {
         InputName(
            name = personUiState.person.firstName,          // State ↓
            onNameChange = viewModel::onFirstNameChange,    // Event ↑
            label = stringResource(R.string.firstname),     // State ↓
            validateName = viewModel::validateFirstname,    // Event ↑
         )
         InputName(
            name = personUiState.person.lastName,           // State ↓
            onNameChange = viewModel::onLastNameChange,     // Event ↑
            label = stringResource(R.string.lastname),      // State ↓
            validateName = viewModel::validateLastname,     // Event ↑
         )
         InputEmail(
            email = personUiState.person.email,             // State ↓
            onEmailChange = viewModel::onEmailChange,       // Event ↑
            validateEmail = viewModel::validateEmail        // Event ↑
         )
         InputPhone(
            phone = personUiState.person.phone,             // State ↓
            onPhoneChange = viewModel::onPhoneChange,       // Event ↑
            validatePhone = viewModel::validatePhone        // Event ↑
         )

         SelectAndShowImage(
            imageUrl = personUiState.person.imageUrl,     // State ↓viewModel.imagePath,                          // State ↓
            onImageUrlChange = viewModel::onImageUrlChange // Event ↑
         )

      } // Column
   } // Scaffold

   val errorState: ErrorUiState by viewModel.errorUiStateFlow.collectAsStateWithLifecycle()
   LaunchedEffect(errorState.params) {
      errorState.params?.let { params: ErrorParams ->
         logDebug(tag, "ErrorUiState: ${errorState.params}")
         // show the error with a snackbar
         showError(snackbarHostState, params, viewModel::navigateTo )
         // reset the errorState, params are copied to showError
         viewModel.onErrorEventHandled()

      }
   }
}