package de.rogallab.mobile.data

import de.rogallab.mobile.data.local.IDataStore
import de.rogallab.mobile.domain.IPeopleRepository
import de.rogallab.mobile.domain.ResultData
import de.rogallab.mobile.domain.entities.Person

class PeopleRepository(
   private val _dataStore: IDataStore
): IPeopleRepository {

   override fun getAll(): ResultData<MutableList<Person>> {
      return try {
         ResultData.Success(_dataStore.selectAll())
      } catch (t: Throwable) {
         ResultData.Error(t)
      }
   }

   override fun getWhere(predicate: (Person) -> Boolean): ResultData<MutableList<Person>> {
      return try {
         ResultData.Success(_dataStore.selectWhere(predicate))
      } catch (t: Throwable) {
         ResultData.Error(t)
      }
   }

   override fun findById(id: String): ResultData<Person?> {
      return try {
//       throw Exception("Fehler Test Exception");
         ResultData.Success(_dataStore.findById(id))
      } catch (t: Throwable) {
         ResultData.Error(t)
      }
   }

   override fun findBy(predicate: (Person) -> Boolean): ResultData<Person?> {
      return try {
         ResultData.Success(_dataStore.findBy(predicate))
      } catch (t: Throwable) {
         ResultData.Error(t)
      }
   }

   override fun create(person: Person): ResultData<Unit> {
      return try {
         _dataStore.insert(person)
         ResultData.Success(Unit)
      } catch (t: Throwable) {
         ResultData.Error(t)
      }
   }

   override fun update(person: Person): ResultData<Unit> {
      return try {
         _dataStore.update(person)
         ResultData.Success(Unit)
      } catch (t: Throwable) {
         ResultData.Error(t)
      }
   }

   override fun remove(person: Person): ResultData<Unit> {
      return try {
         _dataStore.delete(person.id)
         ResultData.Success(Unit)
      } catch (t: Throwable) {
         ResultData.Error(t)
      }
   }

   override fun readDataStore(): ResultData<Unit> {
      return try {
         _dataStore.readDataStore()
         ResultData.Success(Unit)
      } catch (t: Throwable) {
         ResultData.Error(t)
      }
   }

}