package com.stolser.javatraining.webproject.dao.impl.mysql

import java.util.Objects.isNull

import com.stolser.javatraining.webproject.dao._
import com.stolser.javatraining.webproject.dao.exception.DaoException

/**
  * Created by Oleg Stoliarov on 10/14/18.
  */
object MysqlDaoFactory extends DaoFactoryTrait {
	private val CONNECTION_CAN_NOT_BE_NULL = "Connection can not be null."
	private val CONNECTION_IS_NOT_AN_ABSTRACT_CONNECTION_IMPL_FOR_JDBC =
		"Connection is not an AbstractConnectionImpl for JDBC."

	override def getPeriodicalDao(conn: AbstractConnection): PeriodicalDao = {
		checkConnection(conn)
		new MysqlPeriodicalDao(getSqlConnection(conn))
	}

	override def getCredentialDao(conn: AbstractConnection): CredentialDao = {
		checkConnection(conn)
		new MysqlCredentialDao(getSqlConnection(conn))
	}

	override def getUserDao(conn: AbstractConnection): UserDao = {
		checkConnection(conn)
		new MysqlUserDao(getSqlConnection(conn))
	}

	override def getRoleDao(conn: AbstractConnection): RoleDao = {
		checkConnection(conn)
		new MysqlRoleDao(getSqlConnection(conn))
	}

	override def getSubscriptionDao(conn: AbstractConnection): SubscriptionDao = {
		checkConnection(conn)
		new MysqlSubscriptionDao(getSqlConnection(conn))
	}

	override def getInvoiceDao(conn: AbstractConnection): InvoiceDao = {
		checkConnection(conn)
		new MysqlInvoiceDao(getSqlConnection(conn))
	}

	private def checkConnection(conn: AbstractConnection): Unit = {
		if (isNull(conn))
			throw new DaoException(CONNECTION_CAN_NOT_BE_NULL)

		if (!conn.isInstanceOf[AbstractConnectionImpl])
			throw new DaoException(CONNECTION_IS_NOT_AN_ABSTRACT_CONNECTION_IMPL_FOR_JDBC)
	}

	private def getSqlConnection(conn: AbstractConnection) =
		conn.asInstanceOf[AbstractConnectionImpl].connection
}
