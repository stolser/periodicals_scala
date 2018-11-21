package com.ostoliarov.webproject.connection

/**
	* Created by Oleg Stoliarov on 10/13/18.
	* Represents an abstract connection.
	*/
trait AbstractConnection extends AutoCloseable {
	/**
		* Defines begin of a transaction.
		*/
	def beginTransaction(): Unit

	/**
		* Saves a transaction.
		*/
	def commitTransaction(): Unit

	/**
		* rolls back a transaction.
		*/
	def rollbackTransaction(): Unit

	/**
		* Closes a connection.
		*/
	override def close(): Unit
}
