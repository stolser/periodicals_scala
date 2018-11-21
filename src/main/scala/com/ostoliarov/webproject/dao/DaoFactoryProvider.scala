package com.ostoliarov.webproject.dao

import com.ostoliarov.webproject.dao.impl.mysql.MysqlDaoFactory

/**
	* Created by Oleg Stoliarov on 10/13/18.
	*/

object DaoFactoryProvider {
	def mysqlDaoFactory: DaoFactory = MysqlDaoFactory
}
