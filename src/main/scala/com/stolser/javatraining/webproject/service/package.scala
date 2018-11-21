package com.stolser.javatraining.webproject

import com.stolser.javatraining.webproject.connection.pool.ConnectionPool
import com.stolser.javatraining.webproject.dao.AbstractConnection

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
