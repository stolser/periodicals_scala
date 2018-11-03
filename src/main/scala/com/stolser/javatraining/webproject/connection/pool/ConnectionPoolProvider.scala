package com.stolser.javatraining.webproject.connection.pool

import java.io.{FileNotFoundException, IOException, InputStream}
import java.util.Properties

import org.slf4j.LoggerFactory

/**
  * Reads database configuration data and creates a connection pool.
  */
object ConnectionPoolProvider {
	private val LOGGER = LoggerFactory.getLogger(ConnectionPoolProvider.getClass)
	private val DB_CONFIG_FILENAME = "webproject/config/dbConfig.properties"
	private val DB_CONFIG_PARAM_URL = "database.url"
	private val DB_CONFIG_PARAM_DB_NAME = "database.dbName"
	private val DB_CONFIG_PARAM_USER_NAME = "database.userName"
	private val DB_CONFIG_PARAM_USER_PASSWORD = "database.userPassword"
	private val DB_CONFIG_PARAM_MAX_CONN_NUMBER = "database.maxConnNumber"
	private val EXCEPTION_DURING_OPENING_DB_CONFIG_FILE = "Exception during opening the db-config file with path = {}."
	private val EXCEPTION_DURING_LOADING_DB_CONFIG_PROPERTIES = "Exception during loading db-config properties from the file " + "(path = {})"

	lazy val getPool: ConnectionPool = createPoolFromProperties(getProperties(DB_CONFIG_FILENAME))

	private def createPoolFromProperties(properties: Properties): SqlConnectionPool =
		try {
			val url = properties.getProperty(DB_CONFIG_PARAM_URL)
			val dbName = properties.getProperty(DB_CONFIG_PARAM_DB_NAME)
			val userName = properties.getProperty(DB_CONFIG_PARAM_USER_NAME)
			val userPassword = properties.getProperty(DB_CONFIG_PARAM_USER_PASSWORD)
			val maxConnNumber = properties.getProperty(DB_CONFIG_PARAM_MAX_CONN_NUMBER).toInt
			SqlConnectionPool.getBuilder(url, dbName)
				.setUserName(userName)
				.setPassword(userPassword)
				.setMaxConnections(maxConnNumber)
				.build
		} catch {
			case e: FileNotFoundException =>
				LOGGER.error(EXCEPTION_DURING_OPENING_DB_CONFIG_FILE, DB_CONFIG_FILENAME)
				throw new RuntimeException(e)
			case e: IOException =>
				LOGGER.error(EXCEPTION_DURING_LOADING_DB_CONFIG_PROPERTIES, DB_CONFIG_FILENAME)
				throw new RuntimeException(e)
		}

	private def getProperties(dbConfigFileName: String) = {
		val dbConfigFileInput: InputStream = ConnectionPoolProvider.getClass
			.getClassLoader
			.getResourceAsStream(dbConfigFileName)

		val properties = new Properties
		properties.load(dbConfigFileInput)
		properties
	}
}
