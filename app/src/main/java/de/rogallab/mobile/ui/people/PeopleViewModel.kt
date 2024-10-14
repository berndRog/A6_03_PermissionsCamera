package de.rogallab.mobile.ui.people

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.util.Patterns
import androidx.lifecycle.viewModelScope
import de.rogallab.mobile.data.PeopleRepository
import de.rogallab.mobile.data.local.DataStore
import de.rogallab.mobile.data.local.IDataStore
import de.rogallab.mobile.domain.IPeopleRepository
import de.rogallab.mobile.domain.ResultData
import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.domain.utilities.as8
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.ui.ResourceProvider
import de.rogallab.mobile.ui.base.BaseViewModel
import de.rogallab.mobile.ui.errors.ErrorParams
import de.rogallab.mobile.ui.errors.ErrorResources
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PeopleViewModel(
   private val _repository: IPeopleRepository,
   private val _errorResources:ErrorResources
) : BaseViewModel(TAG) {

   private var removedPerson: Person? = null

   init {
      logDebug(TAG, "init")
      _repository.readDataStore()
      viewModelScope.launch {
         delay(100)
         fetchPeople()
      }
   }

   // Data Binding PeopleListScreen <=> PersonViewModel
   private val _peopleUiStateFlow: MutableStateFlow<PeopleUiState> = MutableStateFlow(PeopleUiState())
   val peopleUiStateFlow: StateFlow<PeopleUiState> = _peopleUiStateFlow.asStateFlow()

   // read all people from repository
   fun fetchPeople() {
      logDebug(TAG, "fetchPeople")
      when (val resultData = _repository.getAll()) {
         is ResultData.Success -> {
            _peopleUiStateFlow.update { it: PeopleUiState ->
               it.copy(people = resultData.data.toList())
            }
            logDebug(TAG, "fetchPeople() people.size: ${peopleUiStateFlow.value.people.size}")
         }
         is ResultData.Error -> {
            onErrorEvent(ErrorParams(throwable = resultData.throwable, navEvent = null))
         }
      }
   }

   // Data Binding PersonScreen <=> PersonViewModel
   private val _personUiStateFlow: MutableStateFlow<PersonUiState> = MutableStateFlow(PersonUiState())
   val personUiStateFlow: StateFlow<PersonUiState> = _personUiStateFlow.asStateFlow()

   fun onFirstNameChange(firstName: String) {
      if (firstName == _personUiStateFlow.value.person.firstName) return
      _personUiStateFlow.update { it: PersonUiState ->
         it.copy(person = it.person.copy(firstName = firstName))
      }
   }
   fun onLastNameChange(lastName: String) {
      if (lastName == _personUiStateFlow.value.person.lastName) return
      _personUiStateFlow.update { it: PersonUiState ->
         it.copy(person = it.person.copy(lastName = lastName))
      }
   }
   fun onEmailChange(email: String?) {
      if (email == null || email == _personUiStateFlow.value.person.email) return
      _personUiStateFlow.update { it: PersonUiState ->
         it.copy(person = it.person.copy(email = email))
      }
   }
   fun onPhoneChange(phone: String?) {
      if (phone == null || phone == _personUiStateFlow.value.person.phone) return
      _personUiStateFlow.update { it: PersonUiState ->
         it.copy(person = it.person.copy(phone = phone))
      }
   }

   fun onImageUrlChange(imageUrl: String?) {
      if (imageUrl == null || imageUrl == _personUiStateFlow.value.person.imageUrl) return
      _personUiStateFlow.update { it: PersonUiState ->
         it.copy(person =
            it.person.copy(
               imageUrl = imageUrl,
               localImagePath = imageUrl
            )
         )
      }
   }

   fun fetchPerson(personId: String) {
      logDebug(TAG, "fetchPersonById: $personId")
      when (val resultData = _repository.findById(personId)) {
         is ResultData.Success -> _personUiStateFlow.update { it: PersonUiState ->
            it.copy(person = resultData.data ?: Person())  // new UiState
         }
         is ResultData.Error ->
            onErrorEvent(ErrorParams(throwable = resultData.throwable, navEvent = null))
      }
   }

   fun createPerson() {
      logDebug(TAG, "createPerson: ${_personUiStateFlow.value.person.id.as8()}")
      when (val resultData = _repository.create(_personUiStateFlow.value.person)) {
         is ResultData.Success ->
            fetchPeople()
         is ResultData.Error ->
            onErrorEvent(ErrorParams(throwable = resultData.throwable, navEvent = null))
      }
   }

   fun updatePerson() {
      logDebug(TAG, "updatePerson: ${_personUiStateFlow.value.person.id.as8()}")
      when(val resultData = _repository.update(_personUiStateFlow.value.person)) {
         is ResultData.Success ->
            fetchPeople()
         is ResultData.Error ->
            onErrorEvent(ErrorParams(throwable = resultData.throwable, navEvent = null))
      }
   }

   fun removePerson(person: Person) {
      logDebug(TAG, "removePerson: ${person.id.as8()}")
      when(val resultData = _repository.remove(person)) {
         is ResultData.Success -> {
            removedPerson = person
            fetchPeople()
         }
         is ResultData.Error ->
            onErrorEvent(ErrorParams(throwable = resultData.throwable, navEvent = null))         }
   }

   fun undoRemovePerson() {
      removedPerson?.let { person ->
         logDebug(TAG, "undoRemovePerson: ${person.id.as8()}")
         when(val resultData = _repository.create(person)) {
            is ResultData.Success -> {
               removedPerson = null
               fetchPeople()
            }
            is ResultData.Error ->
               onErrorEvent(ErrorParams(throwable = resultData.throwable, navEvent = null))
         }
      }
   }

   fun clearState() {
      _personUiStateFlow.update { it.copy(person = Person()) }
   }

   fun validateFirstname(name: String): Pair<Boolean, String> =
      if (name.isEmpty() || name.length < _errorResources.charMin)
         Pair(true, _errorResources.firstnameTooShort)
      else if (name.length > _errorResources.charMax )
         Pair(true, _errorResources.firstnameTooLong)
      else
         Pair(false, "")

   fun validateLastname(name: String): Pair<Boolean, String> =
      if (name.isEmpty() || name.length < _errorResources.charMin)
         Pair(true, _errorResources.lastnameTooShort)
      else if (name.length > _errorResources.charMax )
         Pair(true, _errorResources.lastnameTooLong)
      else
         Pair(false, "")

   fun validateEmail(email: String?): Pair<Boolean, String> {
      email?.let {
         when (android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches()) {
            true -> return Pair(false, "") // email ok
            false -> return Pair(true, _errorResources.emailInValid)
         }
      } ?: return Pair(false, "")
   }

   fun validatePhone(phone: String?): Pair<Boolean, String> {
      phone?.let {
         when (Patterns.PHONE.matcher(it).matches()) {
            true -> return Pair(false,"")   // phone ok
            false -> return Pair(true, _errorResources.phoneInValid)
         }
      } ?: return Pair(false, "")
   }


   fun validate(
      isInput: Boolean
   ) {
      // input is ok        -> add and navigate up
      // detail is ok       -> update and navigate up
      // is the is an error -> show error and stay on screen

      val charMin = _errorResources.charMin
      val charMax = _errorResources.charMax

      val person = _personUiStateFlow.value.person

      // firstName or lastName too short or to long
      if (person.firstName.isEmpty() || person.firstName.length < charMin) {
         onErrorEvent(ErrorParams(message = _errorResources.firstnameTooShort, navEvent = null))
      }
      else if (person.firstName.length > charMax) {
         onErrorEvent(ErrorParams(message = _errorResources.firstnameTooLong, navEvent = null))
      }
      else if (person.lastName.isEmpty() || person.lastName.length < charMin) {
         onErrorEvent(ErrorParams(message = _errorResources.lastnameTooShort, navEvent = null))
      }
      else if (person.lastName.length > charMax) {
         onErrorEvent(ErrorParams(message = _errorResources.lastnameTooLong, navEvent = null))
      }

      // email not valid
      else if (person.email != null &&
         !Patterns.EMAIL_ADDRESS.matcher(person.email).matches()) {
         onErrorEvent(ErrorParams(message = _errorResources.emailInValid, navEvent = null))
      }

      // phone not valid
      else if (person.phone != null &&
         !Patterns.PHONE.matcher(person.phone).matches()) {
         onErrorEvent(ErrorParams(message = _errorResources.phoneInValid, navEvent = null))
      }
      else {
         if (isInput) this.createPerson()
         else         this.updatePerson()
      }
   }

   companion object {
      private const val TAG = "[PeopleViewModel]"
   }
}