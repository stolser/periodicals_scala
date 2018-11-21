package com.ostoliarov.webproject.connection.pool

import com.ostoliarov.webproject.connection.AbstractConnection

/**
  * Represents an abstraction for reusable connection pool.
  */
trait ConnectionPool {
	def connection: AbstractConnection
}
