package com.stolser.javatraining.webproject.dao

/**
	* Created by Oleg Stoliarov on 11/8/18.
	*/
trait DaoFactoryTrait {
	def getPeriodicalDao(conn: AbstractConnection): PeriodicalDao

	def getCredentialDao(conn: AbstractConnection): CredentialDao

	def getUserDao(conn: AbstractConnection): UserDao

	def getRoleDao(conn: AbstractConnection): RoleDao

	def getSubscriptionDao(conn: AbstractConnection): SubscriptionDao

	def getInvoiceDao(conn: AbstractConnection): InvoiceDao
}
