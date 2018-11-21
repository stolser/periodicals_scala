package com.stolser.javatraining.webproject.service.impl

import com.stolser.javatraining.webproject.connection.pool.ConnectionPool
import com.stolser.javatraining.webproject.dao.DaoFactory

/**
	* Created by Oleg Stoliarov on 11/21/18.
	*/
trait ServiceDependency {
	implicit val connectionPool: ConnectionPool
	val daoFactory: DaoFactory
}
