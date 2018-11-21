package com.ostoliarov.webproject.dao.impl.mysql

import java.util.Objects.isNull

import com.ostoliarov.webproject.connection.{AbstractConnection, AbstractConnectionImpl}
import com.ostoliarov.webproject.dao._
import com.ostoliarov.webproject.dao.exception.DaoException

/**
	* Created by Oleg Stoliarov on 10/14/18.
	*/
object MysqlDaoFactory extends DaoFactory {
	private val CONNECTION_CAN_NOT_BE_NULL = "Connection can not be null."
	private val CONNECTION_IS_NOT_AN_ABSTRACT_CONNECTION_IMPL_FOR_JDBC =
		"Connection is not an AbstractConnectionImpl for JDBC."

	override def periodicalDao(conn: AbstractConnection): PeriodicalDao = {
		checkConnection(conn)
		new MysqlPeriodicalDao(getSqlConnection(conn))
	}

	override def credentialDao(conn: AbstractConnection): CredentialDao = {
		checkConnection(conn)
		new MysqlCredentialDao(getSqlConnection(conn))
	}

	override def userDao(conn: AbstractConnection): UserDao = {
		checkConnection(conn)
		new MysqlUserDao(getSqlConnection(conn))
	}

	override def roleDao(conn: AbstractConnection): RoleDao = {
		checkConnection(conn)
		new MysqlRoleDao(getSqlConnection(conn))
	}

	override def subscriptionDao(conn: AbstractConnection): SubscriptionDao = {
		checkConnection(conn)
		new MysqlSubscriptionDao(getSqlConnection(conn))
	}

	override def invoiceDao(conn: AbstractConnection): InvoiceDao = {
		checkConnection(conn)
		new MysqlInvoiceDao(getSqlConnection(conn))
	}

	private def checkConnection(conn: AbstractConnection): Unit = {
		if (isNull(conn))
			throw DaoException(CONNECTION_CAN_NOT_BE_NULL)

		if (!conn.isInstanceOf[AbstractConnectionImpl])
			throw DaoException(CONNECTION_IS_NOT_AN_ABSTRACT_CONNECTION_IMPL_FOR_JDBC)
	}

	private def getSqlConnection(conn: AbstractConnection) =
		conn.asInstanceOf[AbstractConnectionImpl].connection
}
