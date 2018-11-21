package com.ostoliarov.webproject.service.impl.mysql

import com.ostoliarov.webproject.connection.pool.{ConnectionPool, ConnectionPoolProvider}
import com.ostoliarov.webproject.dao.{DaoFactory, DaoFactoryProvider}
import com.ostoliarov.webproject.service.impl.{PeriodicalServiceImpl, ServiceDependency}

/**
	* Created by Oleg Stoliarov on 10/15/18.
	*/
object PeriodicalServiceMysqlImpl
	extends PeriodicalServiceImpl
		with ServiceDependency {

	override implicit val connectionPool: ConnectionPool = ConnectionPoolProvider.connectionPoolMysql
	override val daoFactory: DaoFactory = DaoFactoryProvider.mysqlDaoFactory
}
