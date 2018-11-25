package com.ostoliarov.webproject.connection

import java.sql.Connection

import com.ostoliarov.webproject._
import com.ostoliarov.webproject.connection.AbstractConnectionImpl._

/**
	* Created by Oleg Stoliarov on 10/14/18.
	*/

object AbstractConnectionImpl {
	private val CAN_NOT_BEGIN_TRANSACTION = "Can not begin transaction."
	private val CAN_NOT_COMMIT_TRANSACTION = "Can not commit transaction"
	private val CAN_NOT_ROLLBACK_TRANSACTION = "Can not rollback transaction"
	private val CAN_NOT_CLOSE_CONNECTION = "Can not close connection"
}

class AbstractConnectionImpl private[connection](val connection: Connection)
	extends AbstractConnection {

	private var transactionBegun = false
	private var transactionCommitted = false

	override def beginTransaction(): Unit =
		tryAndCatchSqlException(CAN_NOT_BEGIN_TRANSACTION) {
			connection.setAutoCommit(false)
			transactionBegun = true
		}

	override def commitTransaction(): Unit =
		tryAndCatchSqlException(CAN_NOT_COMMIT_TRANSACTION) {
			connection.commit()
			connection.setAutoCommit(true)
			transactionCommitted = true
		}

	override def rollbackTransaction(): Unit =
		tryAndCatchSqlException(CAN_NOT_ROLLBACK_TRANSACTION) {
			connection.rollback()
			connection.setAutoCommit(true)
			transactionCommitted = true
		}

	override def close(): Unit =
		tryAndCatchSqlException(CAN_NOT_CLOSE_CONNECTION) {
			if (transactionBegun && !transactionCommitted)
				rollbackTransaction()
			connection.close()
		}
}

