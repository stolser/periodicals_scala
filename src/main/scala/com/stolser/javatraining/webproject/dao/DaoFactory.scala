package com.stolser.javatraining.webproject.dao

import com.stolser.javatraining.webproject.dao.impl.mysql.MysqlDaoFactory

/**
  * Created by Oleg Stoliarov on 10/13/18.
  */

object DaoFactory {
	def getMysqlDaoFactory: DaoFactory = MysqlDaoFactory
}

abstract class DaoFactory {
	def getPeriodicalDao(conn: AbstractConnection): PeriodicalDao

	def getCredentialDao(conn: AbstractConnection): CredentialDao

	def getUserDao(conn: AbstractConnection): UserDao

	def getRoleDao(conn: AbstractConnection): RoleDao

	def getSubscriptionDao(conn: AbstractConnection): SubscriptionDao

	def getInvoiceDao(conn: AbstractConnection): InvoiceDao
}
