package de.rogallab.mobile.data.local.datastore

import android.content.Context
import de.rogallab.mobile.data.IDataStore
import de.rogallab.mobile.data.local.SeedWithImages
import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.domain.utilities.as8
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logError
import de.rogallab.mobile.domain.utilities.logVerbose
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class DataStore(
   private val _context: Context,
   private val _dispatcherMain: CoroutineDispatcher = Dispatchers.Main,
   private val _dispatcherIO: CoroutineDispatcher = Dispatchers.IO
): IDataStore {

   // list of people
   private var _people: MutableList<Person> = mutableListOf()

   // Json serializer
   private val _json = Json {
      prettyPrint = true
      ignoreUnknownKeys = true
   }

   init {
      logDebug(TAG, "init: read datastore")
      _people.clear()
      // read the dataStore before any other operation!!!
      runBlocking { read() }
   }

   override fun selectAll(): Flow<List<Person>> = flow {
      //delay(500)  // simulate a delay
      emit(_people)
   }.flowOn(_dispatcherIO)

   override fun selectWhere(
      predicate: (Person) -> Boolean
   ): Flow<List<Person>> = flow {
      //delay(500)  // simulate a delay
      emit(_people.filter(predicate).toList())
   }.flowOn(_dispatcherIO)

   override suspend fun findById(id: String): Person? =
      withContext(_dispatcherMain) {
         logDebug(TAG, "findById()")
         _people.firstOrNull { it.id == id }
      }

   override suspend fun findBy(predicate: (Person) -> Boolean): Person? =
      withContext(_dispatcherMain) {
         logDebug(TAG, "findBy()")
         _people.firstOrNull(predicate)
      }

   override suspend fun insert(person: Person) =
      withContext(_dispatcherIO) {
         logDebug(TAG, "insert: $person")
         if (_people.any { it.id == person.id })
            throw IllegalArgumentException("Person with id ${person.id} already exists")
         _people.add(person)
         write()
      }


   override suspend fun update(person: Person) =
      withContext(_dispatcherIO) {
         logDebug(TAG, "update()")
         val index = _people.indexOfFirst { it.id == person.id }
         if (index == -1)
            throw IllegalArgumentException("Person with id ${person.id} does not exist")
         _people[index] = person
         write()
      }

   override suspend fun delete(person: Person) =
      withContext(_dispatcherIO) {
         logDebug(TAG, "delete()")
         if (_people.none { it.id == person.id })
            throw IllegalArgumentException("Person with id ${person.id} does not exist")
         _people.remove(person)
         write()
      }

   // dataStore is saved as JSON file to the user's home directory
   // UserHome/Documents/android/people.json
   private suspend fun read() {
      try {
         val filePath = getFilePath(FILE_NAME)
         val file = File(filePath)
         logVerbose(TAG, "JSON path $filePath")

         // no file or empty file, seed the data
         if (!file.exists() || file.readText().isBlank()) {
            // seed _people with some data
            withContext(_dispatcherIO) {
               val seed = SeedWithImages(_context, _context.resources)
               _people.addAll(seed.people)
               logVerbose(TAG, "create(): seedData ${_people.size} people")
               write()  // no return value needed
            }
            return@read
         }

         // read the JSON file asynchronously
         withContext(_dispatcherIO) {
            logDebug(TAG,"read JSON")
            val jsonString = file.readText()
            if (jsonString.isNotBlank()) {
               _people =  _json.decodeFromString(jsonString)
            }
            logDebug(TAG, "read(): decode JSON ${_people.size} people")
         }
      } catch (e: Exception) {
         logError(TAG, "Failed to read: ${e.message}")
         throw e
      }
   }

   // write the list of people to the dataStore
   private suspend fun write() {
      try {
         val filePath = getFilePath(FILE_NAME)
         // encode JSON asynchronously
         val jsonString = withContext(_dispatcherMain) {
            logDebug(TAG, "encode JSON")
            _json.encodeToString(_people)
         }
         logDebug(TAG, "write(): encode JSON ${_people.size} people")
         // save to a file asynchronously
         val file = File(filePath)
         withContext(_dispatcherIO) {
            file.writeText(jsonString)
         }
         logVerbose(TAG, jsonString)
      } catch (e: Exception) {
         logError(TAG, "Failed to write: ${e.message}")
         throw e
      }
   }

   // get the file path for the dataStore
   // UserHome/Documents/android/people.json
   private fun getFilePath(fileName: String): String {
      try {
         // get the Apps home directory
         val appHome = _context.filesDir
         // the directory must exist, if not create it
         val directoryPath = "$appHome/documents/$DIRECTORY_NAME"
         if ( !directoryExists(directoryPath) )
            createDirectory(directoryPath)
         // return the file path
         return "$directoryPath/$fileName"
      } catch (e: Exception) {
         logError(TAG, "Failed to getFilePath or create directory; ${e.localizedMessage}")
         throw e
      }
   }

   private fun directoryExists(directoryPath: String): Boolean {
      val directory = File(directoryPath)
      return directory.exists() && directory.isDirectory
   }

   private fun createDirectory(directoryPath: String): Boolean {
      val directory = File(directoryPath)
      return directory.mkdirs()
   }

   companion object {
      private const val TAG = "<-DataStore"
      private const val DIRECTORY_NAME = "android"
      private const val FILE_NAME = "people3.json"
   }
}