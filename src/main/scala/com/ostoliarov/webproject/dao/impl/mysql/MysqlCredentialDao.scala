package com.ostoliarov.webproject.dao.impl.mysql

import java.sql.{Connection, PreparedStatement, ResultSet}

import com.ostoliarov.webproject._
import com.ostoliarov.webproject.dao.CredentialDao
import com.ostoliarov.webproject.dao.impl.mysql.MysqlCredentialDao._
import com.ostoliarov.webproject.model.entity.user.Credential

/**
	* Created by Oleg Stoliarov on 10/14/18.
	*/

object MysqlCredentialDao {
	private val DB_CREDENTIALS_ID = "credentials.id"
	private[mysql] val DB_CREDENTIALS_USER_NAME = "credentials.user_name"
	private val DB_CREDENTIALS_PASSWORD_HASH = "credentials.password_hash"
	private val EXCEPTION_DURING_CREATING_CREDENTIAL = "Exception during creating a credential."
}

class MysqlCredentialDao private[mysql](conn: Connection) extends CredentialDao {

	override def findCredentialByUserName(userName: String): Option[Credential] = {
		val sqlStatement = "SELECT * FROM credentials WHERE user_name = ?"
		val exceptionMessage = s"Exception during execution statement '$sqlStatement' for userName = $userName."

		tryAndCatchSqlException(exceptionMessage) {
			withResources(conn.prepareStatement(sqlStatement)) {
				st: PreparedStatement => {
					st.setString(1, userName)

					withResources(st.executeQuery()) {
						rs: ResultSet =>
							if (rs.next())
								Some(getCredentialFromRs(rs))
							else None
					}
				}
			}
		}
	}

	private def getCredentialFromRs(rs: ResultSet): Credential =
		Credential(
			id = rs.getLong(DB_CREDENTIALS_ID),
			userName = rs.getString(DB_CREDENTIALS_USER_NAME),
			passwordHash = rs.getString(DB_CREDENTIALS_PASSWORD_HASH)
		)

	override def createNew(credential: Credential): Boolean = {
		val sqlStatement = "INSERT INTO credentials (user_name, password_hash, user_id) " +
			"VALUES (?, ?, ?)"

		tryAndCatchSqlException(EXCEPTION_DURING_CREATING_CREDENTIAL) {
			withResources(conn.prepareStatement(sqlStatement)) {
				st: PreparedStatement => {
					st.setString(1, credential.userName)
					st.setString(2, credential.passwordHash)
					st.setLong(3, credential.userId)

					st.executeUpdate() > 0
				}
			}
		}
	}
}
