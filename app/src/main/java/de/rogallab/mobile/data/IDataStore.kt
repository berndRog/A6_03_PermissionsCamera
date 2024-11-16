package de.rogallab.mobile.data

import de.rogallab.mobile.domain.entities.Person
import kotlinx.coroutines.flow.Flow

interface IDataStore {
   fun selectAll(): Flow<List<Person>>
   fun selectWhere(predicate: (Person) -> Boolean): Flow<List<Person>>
   suspend fun findById(id: String): Person?
   suspend fun findBy(predicate: (Person) -> Boolean): Person?

   suspend fun insert(person: Person)
   suspend fun update(person: Person)
   suspend fun delete(person: Person)
}