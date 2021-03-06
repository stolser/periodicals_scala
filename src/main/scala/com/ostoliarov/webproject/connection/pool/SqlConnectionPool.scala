package com.ostoliarov.webproject.connection.pool

import com.ostoliarov.webproject._
import com.ostoliarov.webproject.connection.pool.SqlConnectionPool._
import com.ostoliarov.webproject.connection.{AbstractConnection, AbstractConnectionImpl}
import javax.sql.DataSource
import org.apache.commons.dbcp2.BasicDataSource

/**
  * Created by Oleg Stoliarov on 10/6/18.
  */
class SqlConnectionPool private(val builder: Builder) extends ConnectionPool {
	private val dataSource: DataSource = basicDataSource
	private val description = builder.url + builder.dbName

	override def connection: AbstractConnection =
		tryAndCatchSqlException(CONNECTION_EXCEPTION_TEXT) {
			new AbstractConnectionImpl(dataSource.getConnection)
		}

	override def toString: String = description

	private def basicDataSource = {
		val dataSource = new BasicDataSource
		dataSource.setDriverClassName(builder.driverClassName)
		dataSource.setUrl(generateUrl(builder))
		dataSource.setUsername(builder.userName)
		dataSource.setPassword(builder.password)
		dataSource.setMaxTotal(builder.maxConnections)
		dataSource
	}

	private def generateUrl(builder: SqlConnectionPool.Builder) = {
		var url = builder.url + builder.dbName
		if (!builder.useSsl) url += USE_SSL_FALSE
		url
	}
}

object SqlConnectionPool {
	private val USER_NAME_DEFAULT = "test"
	private val USER_PASSWORD_DEFAULT = "test"
	private val DRIVER_NAME_DEFAULT = "com.mysql.cj.jdbc.Driver"
	private val MAX_TOTAL_CONNECTIONS = 10
	private val USE_SSL_FALSE = "?useSSL=false"
	private val CONNECTION_EXCEPTION_TEXT = "Exception during getting a connection from a dataSource."
	private val URL_SHOULD_NOT_BE_NULL = "url should not be null."
	private val DB_NAME_SHOULD_NOT_BE_NULL = "dbName should not be null."
	private val DRIVER_CLASS_NAME_SHOULD_NOT_BE_NULL = "driverClassName should not be null."
	private val USER_NAME_SHOULD_NOT_BE_NULL = "userName should not be null."
	private val PASSWORD_SHOULD_NOT_BE_NULL = "password should not be null."
	private val MAX_CONNECTIONS_SHOULD_BE_A_POSITIVE_NUMBER = "maxConnections should be a positive number."

	def builder(url: String, dbName: String) = new this.Builder(url, dbName)

	class Builder(val url: String, val dbName: String) {
		require(url != null, URL_SHOULD_NOT_BE_NULL)
		require(dbName != null, DB_NAME_SHOULD_NOT_BE_NULL)
		private[SqlConnectionPool] var driverClassName = DRIVER_NAME_DEFAULT
		private[SqlConnectionPool] var userName = USER_NAME_DEFAULT
		private[SqlConnectionPool] var password = USER_PASSWORD_DEFAULT
		private[SqlConnectionPool] var maxConnections = MAX_TOTAL_CONNECTIONS
		private[SqlConnectionPool] var useSsl = false

		def withDriverClassName(driverClassName: String): Builder = {
			require(driverClassName != null, DRIVER_CLASS_NAME_SHOULD_NOT_BE_NULL)
			this.driverClassName = driverClassName
			this
		}

		def withUserName(userName: String): Builder = {
			require(userName != null, USER_NAME_SHOULD_NOT_BE_NULL)
			this.userName = userName
			this
		}

		def withPassword(password: String): Builder = {
			require(password != null, PASSWORD_SHOULD_NOT_BE_NULL)
			this.password = password
			this
		}

		def withSslUsage(useSsl: Boolean): Builder = {
			this.useSsl = useSsl
			this
		}

		def withMaxConnections(maxConnections: Int): Builder = {
			require(maxConnections > 0, MAX_CONNECTIONS_SHOULD_BE_A_POSITIVE_NUMBER)
			this.maxConnections = maxConnections
			this
		}

		def build = new SqlConnectionPool(this)
	}
}
