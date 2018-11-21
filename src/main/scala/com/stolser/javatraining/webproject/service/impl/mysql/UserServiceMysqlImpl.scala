package com.stolser.javatraining.webproject.service.impl.mysql

import com.stolser.javatraining.webproject.connection.pool.{ConnectionPool, ConnectionPoolProvider}
import com.stolser.javatraining.webproject.dao.{DaoFactory, DaoFactoryProvider}
import com.stolser.javatraining.webproject.service.impl.{ServiceDependency, UserServiceImpl}

/**
	* Created by Oleg Stoliarov on 10/15/18.
	*/
object UserServiceMysqlImpl
	extends UserServiceImpl
		with ServiceDependency {

	override implicit val connectionPool: ConnectionPool = ConnectionPoolProvider.connectionPoolMysql
	override val daoFactory: DaoFactory = DaoFactoryProvider.mysqlDaoFactory
}
