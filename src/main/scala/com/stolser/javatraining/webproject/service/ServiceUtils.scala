package com.stolser.javatraining.webproject.service

import com.stolser.javatraining.webproject.connection.pool.ConnectionPool
import com.stolser.javatraining.webproject.dao.AbstractConnection
import com.stolser.javatraining.webproject.utils.TryCatchUtils.withResources

/**
  * Created by Oleg Stoliarov on 10/15/18.
  */
object ServiceUtils {
	private[service] def withConnection[A](block: (AbstractConnection) => A)
										  (implicit connectionPool: ConnectionPool) = {
		withResources(connectionPool.getConnection) {
			conn: AbstractConnection =>
				block(conn)
		}
	}
}
