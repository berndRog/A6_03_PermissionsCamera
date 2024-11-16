package de.rogallab.mobile.data.repositories

import de.rogallab.mobile.data.IDataStore
import de.rogallab.mobile.domain.IPeopleRepository
import de.rogallab.mobile.domain.ResultData
import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.domain.utilities.as8
import de.rogallab.mobile.domain.utilities.logDebug
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

import kotlinx.coroutines.withContext

class PeopleRepository(
   private val _dataStore: IDataStore,
   private val _coroutineDispatcher: CoroutineDispatcher
): IPeopleRepository {

   override fun getAll(): Flow<ResultData<List<Person>>> = flow {
      try {
         _dataStore.selectAll().collect { it: List<Person> ->
            logDebug(TAG, "getAll: ${it.size}")
            emit(ResultData.Success(it))
         }
      } catch (t: Throwable) {
         emit(ResultData.Error(t))
      }
   }.flowOn(_coroutineDispatcher)

   override fun getWhere(
      predicate: (Person) -> Boolean
   ): Flow<ResultData<List<Person>>> = flow {
      try {
         _dataStore.selectWhere(predicate).collect { it: List<Person> ->
            logDebug(TAG, "getWhere: ${it.size}")
            emit(ResultData.Success(it))
         }
      } catch (t: Throwable) {
         emit(ResultData.Error(t))
      }
   }.flowOn(_coroutineDispatcher)


   override suspend fun getById(id: String): ResultData<Person?> =
      withContext(_coroutineDispatcher) {
         return@withContext try {
            logDebug(TAG, "getById: $id.as8()")
            ResultData.Success(_dataStore.findById(id))
         } catch (t: Throwable) {
            ResultData.Error(t)
         }
      }

   override suspend fun getBy(
      predicate: (Person) -> Boolean
   ): ResultData<Person?> =
      withContext(_coroutineDispatcher) {
         return@withContext try {
            logDebug(TAG, "getBy: $predicate")
            ResultData.Success(_dataStore.findBy(predicate))
         } catch (t: Throwable) {
            ResultData.Error(t)
         }
      }

   override suspend fun create(person: Person): ResultData<Unit> =
      withContext(_coroutineDispatcher) {
         return@withContext try {
            logDebug(TAG, "create: ${person.id.as8()}")
            _dataStore.insert(person)
            ResultData.Success(Unit)
         } catch (t: Throwable) {
            ResultData.Error(t)
         }
      }

   override suspend fun update(person: Person): ResultData<Unit> =
      withContext(_coroutineDispatcher) {
         return@withContext try {
            logDebug(TAG, "update: ${person.id.as8()}")
            _dataStore.update(person)
            ResultData.Success(Unit)
         } catch (t: Throwable) {
            ResultData.Error(t)
         }
      }

   override suspend fun remove(person: Person): ResultData<Unit> =
      withContext(_coroutineDispatcher) {
         return@withContext try {
            logDebug(TAG, "remove: ${person.id.as8()}")
            _dataStore.delete(person)
            ResultData.Success(Unit)
         } catch (t: Throwable) {
            ResultData.Error(t)
         }
      }

   companion object {
      private const val TAG = "<-PeopleRepository"
   }
}