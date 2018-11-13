package com.stolser.javatraining.webproject.dao

/**
  * Created by Oleg Stoliarov on 10/13/18.
  */
trait GenericDao[A] {
	/**
	  * Retrieves an entity by its id.
	  *
	  * @param id must not be null
	  * @return the entity with the given id or { @code null} if none found
	  */
	def findOneById(id: Long): Option[A]

	/**
	  * Returns all entities from the db.
	  *
	  * @return all entities
	  */
	def findAll: List[A]

	/**
	  * Creates a new entity taking values for the fields from the passed entity.
	  * If a passed entity has the 'id' field it is ignored.
	  *
	  * @param entity an object to be persisted
	  * @return a persisted entity's id
	  * @throws IllegalArgumentException in case the given entity is null
	  */
	def createNew(entity: A): Long

	/**
	  * Updates an entity in the db. If the passed entity has such an 'id' that there is no
	  * entity in the db with it, the method throws a { @link java.util.NoSuchElementException}.
	  *
	  * @param entity an object to be updated
	  * @return an updated entity
	  */
	def update(entity: A): Int
}
