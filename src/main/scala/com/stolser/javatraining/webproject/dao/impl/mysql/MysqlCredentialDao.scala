package com.stolser.javatraining.webproject.dao.impl.mysql

import java.sql.{Connection, PreparedStatement, ResultSet, SQLException}

import com.stolser.javatraining.webproject.dao.DaoUtils.tryAndCatchSqlException
import com.stolser.javatraining.webproject.utils.TryWithResources.withResources
import com.stolser.javatraining.webproject.dao.{CredentialDao, DaoUtils}
import com.stolser.javatraining.webproject.dao.exception.DaoException
import com.stolser.javatraining.webproject.model.entity.user.Credential

/**
  * Created by Oleg Stoliarov on 10/14/18.
  */

object MysqlCredentialDao {
	private val DB_CREDENTIALS_ID = "credentials.id"
	private[mysql] val DB_CREDENTIALS_USER_NAME = "credentials.user_name"
	private val DB_CREDENTIALS_PASSWORD_HASH = "credentials.password_hash"
	private val EXCEPTION_DURING_CREATING_CREDENTIAL = "Exception during creating a credential."
}

class MysqlCredentialDao(conn: Connection) extends CredentialDao {

	import MysqlCredentialDao._

	override def findCredentialByUserName(userName: String): Credential = {
		val sqlStatement = "SELECT * FROM credentials WHERE user_name = ?"
		val exceptionMessage = s"Exception during execution statement '$sqlStatement' for userName = $userName."

		tryAndCatchSqlException(exceptionMessage) { () =>
			withResources(conn.prepareStatement(sqlStatement)) {
				st: PreparedStatement => {
					st.setString(1, userName)

					withResources(st.executeQuery()) {
						rs: ResultSet => {
							if (rs.next())
								getCredentialFromResultSet(rs)
							else
								null
						}
					}
				}
			}
		}
	}

	def getCredentialFromResultSet(rs: ResultSet): Credential =
		Credential(
			id = rs.getLong(DB_CREDENTIALS_ID),
			userName = rs.getString(DB_CREDENTIALS_USER_NAME),
			passwordHash = rs.getString(DB_CREDENTIALS_PASSWORD_HASH)
		)

	override def createNew(credential: Credential): Boolean = {
		val sqlStatement = "INSERT INTO credentials (user_name, password_hash, user_id) " +
			"VALUES (?, ?, ?)"

		tryAndCatchSqlException(EXCEPTION_DURING_CREATING_CREDENTIAL) { () =>
			withResources(conn.prepareStatement(sqlStatement)) {
				st: PreparedStatement => {
					st.setString(1, credential.getUserName)
					st.setString(2, credential.getPasswordHash)
					st.setLong(3, credential.getUserId)

					st.executeUpdate() > 0
				}
			}
		}
	}
}
