package de.rogallab.mobile.ui.people.composables

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.safeGestures
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.rogallab.mobile.R
import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.ui.errors.ErrorParams
import de.rogallab.mobile.ui.errors.ErrorUiState
import de.rogallab.mobile.ui.errors.showError
import de.rogallab.mobile.ui.navigation.NavEvent
import de.rogallab.mobile.ui.navigation.NavScreen
import de.rogallab.mobile.ui.people.PeopleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleListScreen(
   viewModel: PeopleViewModel
) {
   val tag = "[PeopleListScreen]"

   // Observe the peopleUiState of the viewmodel
   val peopleUiState by viewModel.peopleUiStateFlow.collectAsStateWithLifecycle()

   val activity = LocalContext.current as Activity

   // Back navigation
   BackHandler(
      enabled = true,
      onBack = {
         activity.finish()
      }
   )

   val windowInsets = WindowInsets.systemBars
      .add(WindowInsets.safeGestures)

   val snackbarHostState = remember { SnackbarHostState() }

   Scaffold(
      modifier = Modifier
         .fillMaxSize()
         .padding(windowInsets.asPaddingValues())
         .background(color = MaterialTheme.colorScheme.surface),
      topBar = {
         TopAppBar(
            title = { Text(text = stringResource(R.string.people_list)) },
            navigationIcon = {
               IconButton(
                  onClick = {
                     logDebug(tag, "Lateral Navigation: finish app")
                     // Finish the app
                     activity.finish()
                  }
               ) {
                  Icon(imageVector = Icons.Default.Menu,
                     contentDescription = stringResource(R.string.back))
               }
            }
         )
      },
      floatingActionButton = {
         FloatingActionButton(
            containerColor = MaterialTheme.colorScheme.tertiary,
            onClick = {
               // FAB clicked -> InputScreen initialized
               viewModel.clearState()
               logInfo(tag, "Forward Navigation: FAB clicked")
               viewModel.navigateTo(NavEvent.NavigateTo(NavScreen.PersonInput.route))
            }
         ) {
            Icon(Icons.Default.Add, "Add a contact")
         }
      },
      snackbarHost = {
         SnackbarHost(hostState = snackbarHostState) { data ->
            Snackbar(snackbarData = data, actionOnNewLine = true)
         }
      }
   ) { paddingValues: PaddingValues ->

      val items = peopleUiState.people.sortedBy { it.firstName }

      LazyColumn(
         modifier = Modifier
            .padding(paddingValues = paddingValues)
      ) {
         items(
            items = items,
            key = { person: Person -> person.id }
         ) { person ->
            PersonListItem(
               firstName = person.firstName,
               lastName = person.lastName,
               email = person.email,
               phone = person.phone,
               imagePath = person.imageUrl,
               onClick = {
                  logDebug(tag, "Forward Navigation: PersonDetail")
                  viewModel.navigateTo(
                     NavEvent.NavigateTo(NavScreen.PersonDetail.route+"/${person.id}"))
               }

            )
         }
      }
   }

   val errorState: ErrorUiState by viewModel.errorUiStateFlow.collectAsStateWithLifecycle()
   LaunchedEffect(errorState.params) {
      errorState.params?.let { params: ErrorParams ->
         logDebug(tag, "ErrorUiState: ${errorState.params}")
         // close the keyboard
         //val ime = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
         //ime.hideSoftInputFromWindow(view.windowToken, 0)

         // show the error with a snackbar
         showError(snackbarHostState, params, viewModel::navigateTo )

         // reset the errorState, params are copied to showError
         viewModel.onErrorEventHandled()

      }
   }
}