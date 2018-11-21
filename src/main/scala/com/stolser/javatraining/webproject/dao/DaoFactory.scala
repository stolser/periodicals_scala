package com.stolser.javatraining.webproject.dao

/**
	* Created by Oleg Stoliarov on 11/8/18.
	*/
trait DaoFactory {
	def periodicalDao(conn: AbstractConnection): PeriodicalDao

	def credentialDao(conn: AbstractConnection): CredentialDao

	def userDao(conn: AbstractConnection): UserDao

	def roleDao(conn: AbstractConnection): RoleDao

	def subscriptionDao(conn: AbstractConnection): SubscriptionDao

	def invoiceDao(conn: AbstractConnection): InvoiceDao
}
