package com.ostoliarov.webproject.service.impl

import com.ostoliarov.webproject.connection.pool.ConnectionPool
import com.ostoliarov.webproject.dao.DaoFactory

/**
	* Created by Oleg Stoliarov on 11/21/18.
	*/
trait ServiceDependency {
	implicit val connectionPool: ConnectionPool
	val daoFactory: DaoFactory
}
