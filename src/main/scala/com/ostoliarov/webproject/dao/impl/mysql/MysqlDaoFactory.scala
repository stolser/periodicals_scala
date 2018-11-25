package com.ostoliarov.webproject.dao.impl.mysql

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
		new MysqlPeriodicalDao(sqlConnection(conn))
	}

	override def credentialDao(conn: AbstractConnection): CredentialDao = {
		checkConnection(conn)
		new MysqlCredentialDao(sqlConnection(conn))
	}

	override def userDao(conn: AbstractConnection): UserDao = {
		checkConnection(conn)
		new MysqlUserDao(sqlConnection(conn))
	}

	override def roleDao(conn: AbstractConnection): RoleDao = {
		checkConnection(conn)
		new MysqlRoleDao(sqlConnection(conn))
	}

	override def subscriptionDao(conn: AbstractConnection): SubscriptionDao = {
		checkConnection(conn)
		new MysqlSubscriptionDao(sqlConnection(conn))
	}

	override def invoiceDao(conn: AbstractConnection): InvoiceDao = {
		checkConnection(conn)
		new MysqlInvoiceDao(sqlConnection(conn))
	}

	private def checkConnection(conn: AbstractConnection): Unit = {
		require(conn != null, CONNECTION_CAN_NOT_BE_NULL)

		if (!conn.isInstanceOf[AbstractConnectionImpl])
			throw DaoException(CONNECTION_IS_NOT_AN_ABSTRACT_CONNECTION_IMPL_FOR_JDBC)
	}

	private def sqlConnection(conn: AbstractConnection) =
		conn.asInstanceOf[AbstractConnectionImpl].connection
}
