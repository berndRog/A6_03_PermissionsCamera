package de.rogallab.mobile.domain

import de.rogallab.mobile.domain.entities.Person
import kotlinx.coroutines.flow.Flow

interface IPeopleRepository {

    fun getAll(): Flow<ResultData<List<Person>>>
    fun getWhere(predicate: (Person) -> Boolean): Flow<ResultData<List<Person>>>
    suspend fun getById(id: String): ResultData<Person?>
    suspend fun getBy(predicate: (Person) -> Boolean): ResultData<Person?>

    suspend fun create(person: Person): ResultData<Unit>
    suspend fun update(person: Person): ResultData<Unit>
    suspend fun remove(person: Person): ResultData<Unit>

}