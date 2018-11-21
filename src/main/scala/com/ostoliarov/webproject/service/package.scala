package com.ostoliarov.webproject

import com.ostoliarov.webproject.connection.AbstractConnection
import com.ostoliarov.webproject.connection.pool.ConnectionPool

/**
	* Created by Oleg Stoliarov on 11/21/18.
	*/
package object service {
	def withConnection[A](block: (AbstractConnection) => A)
											 (implicit connectionPool: ConnectionPool): A = {
		withResources(connectionPool.connection) {
			conn: AbstractConnection =>
				block(conn)
		}
	}
}
