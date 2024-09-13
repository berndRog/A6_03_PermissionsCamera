package de.rogallab.mobile.data

import de.rogallab.mobile.data.local.IDataStore
import de.rogallab.mobile.domain.IPeopleRepository
import de.rogallab.mobile.domain.ResultData
import de.rogallab.mobile.domain.entities.Person

class PeopleRepository(
   val dataStore: IDataStore
): IPeopleRepository {



   override fun getAll(): ResultData<MutableList<Person>> {
      return try {
         ResultData.Success(dataStore.selectAll())
      } catch (t: Throwable) {
         ResultData.Error(t)
      }
   }

   override fun getWhere(predicate: (Person) -> Boolean): ResultData<MutableList<Person>> {
      return try {
         ResultData.Success(dataStore.selectWhere(predicate))
      } catch (t: Throwable) {
         ResultData.Error(t)
      }
   }

   override fun findById(id: String): ResultData<Person?> {
      return try {
//       throw Exception("Fehler Test Exception");
         ResultData.Success(dataStore.findById(id))
      } catch (t: Throwable) {
         ResultData.Error(t)
      }
   }

   override fun findBy(predicate: (Person) -> Boolean): ResultData<Person?> {
      return try {
         ResultData.Success(dataStore.findBy(predicate))
      } catch (t: Throwable) {
         ResultData.Error(t)
      }
   }

   override fun create(person: Person): ResultData<Unit> {
      return try {
         dataStore.insert(person)
         ResultData.Success(Unit)
      } catch (t: Throwable) {
         ResultData.Error(t)
      }
   }

   override fun update(person: Person): ResultData<Unit> {
      return try {
         dataStore.update(person)
         ResultData.Success(Unit)
      } catch (t: Throwable) {
         ResultData.Error(t)
      }
   }

   override fun remove(person: Person): ResultData<Unit> {
      return try {
         dataStore.delete(person.id)
         ResultData.Success(Unit)
      } catch (t: Throwable) {
         ResultData.Error(t)
      }
   }

   override fun readDataStore(): ResultData<Unit> {
      return try {
         dataStore.readDataStore()
         ResultData.Success(Unit)
      } catch (t: Throwable) {
         ResultData.Error(t)
      }
   }

}