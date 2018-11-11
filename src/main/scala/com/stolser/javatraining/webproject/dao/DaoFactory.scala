package com.stolser.javatraining.webproject.dao

import com.stolser.javatraining.webproject.dao.impl.mysql.MysqlDaoFactory

/**
	* Created by Oleg Stoliarov on 10/13/18.
	*/

object DaoFactory {
	def getMysqlDaoFactory: DaoFactoryTrait = MysqlDaoFactory
}
