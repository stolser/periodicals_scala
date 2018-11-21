package com.ostoliarov.webproject.dao.impl.mysql

import java.sql
import java.sql._
import java.util.Date
import java.util.Objects.nonNull

import com.ostoliarov.webproject._
import com.ostoliarov.webproject.dao.UserDao
import com.ostoliarov.webproject.dao.exception.DaoException
import com.ostoliarov.webproject.dao.impl.mysql.MysqlUserDao._
import com.ostoliarov.webproject.model.entity.user.{User, UserStatus}

import scala.collection.mutable

/**
	* Created by Oleg Stoliarov on 10/14/18.
	*/

object MysqlUserDao {
	private val DB_USERS_ID = "users.id"
	private val DB_USERS_FIRST_NAME = "users.first_name"
	private val DB_USERS_LAST_NAME = "users.last_name"
	private val DB_USERS_BIRTHDAY = "users.birthday"
	private val DB_USERS_EMAIL = "users.email"
	private val DB_USERS_ADDRESS = "users.address"
	private val DB_USERS_STATUS = "users.status"
	private val EXCEPTION_DURING_FINDING_ALL_USERS = "Exception during finding all users."
	private val EXCEPTION_DURING_FINDING_USER_BY_NAME = "Exception during finding a user with userName = %s."
	private val EXCEPTION_DURING_FINDING_USER_BY_ID = "Exception during finding a user with id = %d."
	private val EXCEPTION_DURING_CREATING_NEW_USER = "Exception during creating a new user: %s"
	private val CREATING_USER_FAILED_NO_ROWS_AFFECTED = "Creating user (%s) failed, no rows affected."
	private val SELECT_COUNT_FROM_USERS_WHERE_EMAIL = "SELECT COUNT(id) FROM users WHERE users.email = ?"
}

class MysqlUserDao private[mysql](conn: Connection) extends UserDao {

	override def findOneById(id: Long): Option[User] = {
		val sqlStatement = "SELECT * FROM users " +
			"JOIN credentials ON (users.id = credentials.user_id) " +
			"WHERE users.id = ?"

		tryAndCatchSqlException(exceptionMessage = EXCEPTION_DURING_FINDING_USER_BY_ID.format(id)) { () =>
			withResources(conn.prepareStatement(sqlStatement)) {
				st: PreparedStatement => {
					st.setLong(1, id)

					withResources(st.executeQuery()) {
						rs: ResultSet =>
							if (rs.next)
								Some(getUserFromResults(rs))
							else None
					}
				}
			}
		}
	}

	override def findOneByUserName(userName: String): Option[User] = {
		val sqlStatement = "SELECT * FROM credentials " +
			"INNER JOIN users ON (credentials.user_id = users.id) " +
			"WHERE credentials.user_name = ?"

		tryAndCatchSqlException(exceptionMessage = EXCEPTION_DURING_FINDING_USER_BY_NAME.format(userName)) { () =>
			withResources(conn.prepareStatement(sqlStatement)) {
				st: PreparedStatement => {
					st.setString(1, userName)

					withResources(st.executeQuery()) {
						rs: ResultSet =>
							if (rs.next)
								Some(getUserFromResults(rs))
							else None
					}
				}
			}
		}
	}

	override def emailExistsInDb(email: String): Boolean =
		tryAndCatchSqlException() { () =>
			withResources(conn.prepareStatement(SELECT_COUNT_FROM_USERS_WHERE_EMAIL)) {
				st: PreparedStatement => {
					st.setString(1, email)

					withResources(st.executeQuery()) {
						rs: ResultSet =>
							rs.next && rs.getInt(1) > 0
					}
				}
			}
		}

	override def findAll: List[User] = {
		val sqlStatement = "SELECT * FROM credentials " +
			"RIGHT OUTER JOIN users ON (credentials.user_id = users.id)"

		tryAndCatchSqlException(exceptionMessage = EXCEPTION_DURING_FINDING_ALL_USERS) { () =>
			withResources(conn.prepareStatement(sqlStatement)) {
				st: PreparedStatement => {
					withResources(st.executeQuery()) {
						rs: ResultSet =>
							val users = mutable.Buffer[User]()

							while (rs.next)
								users += getUserFromResults(rs)

							users.toList
					}
				}
			}
		}
	}

	@throws[SQLException]
	private def getUserFromResults(rs: ResultSet) =
		User(
			id = rs.getLong(DB_USERS_ID),
			userName = rs.getString(MysqlCredentialDao.DB_CREDENTIALS_USER_NAME),
			firstName = Option(rs.getString(DB_USERS_FIRST_NAME)),
			lastName = Option(rs.getString(DB_USERS_LAST_NAME)),
			birthday = getBirthdayFromRs(rs),
			email = rs.getString(DB_USERS_EMAIL),
			address = Option(rs.getString(DB_USERS_ADDRESS)),
			status = UserStatus.withName(rs.getString(DB_USERS_STATUS).toUpperCase)
		)

	@throws[SQLException]
	private def getBirthdayFromRs(rs: ResultSet) = {
		val birthday = rs.getDate(DB_USERS_BIRTHDAY)

		if (nonNull(birthday))
			Some(new Date(birthday.getTime))
		else
			None
	}

	override def createNew(user: User): Long = {
		val exceptionMessage = EXCEPTION_DURING_CREATING_NEW_USER.format(user)
		val sqlStatement = "INSERT INTO users " +
			"(first_name, last_name, birthday, email, address, status) " +
			"VALUES (?, ?, ?, ?, ?, ?)"

		tryAndCatchSqlException(exceptionMessage) { () =>
			withResources(conn.prepareStatement(sqlStatement, Statement.RETURN_GENERATED_KEYS)) {
				st: PreparedStatement => {
					st.setString(1, user.firstName.getOrElse(""))
					st.setString(2, user.lastName.getOrElse(""))
					st.setDate(3, getBirthdayFromUser(user))
					st.setString(4, user.email)
					st.setString(5, user.address.getOrElse(""))
					st.setString(6, user.status.toString.toLowerCase)

					tryExecuteUpdate(st, exceptionMessage)

					tryRetrieveId(st, exceptionMessageNoRows = CREATING_USER_FAILED_NO_ROWS_AFFECTED.format(user))
				}
			}
		}
	}

	@throws[SQLException]
	private def tryExecuteUpdate(st: PreparedStatement,
															 exceptionMessage: String): Unit = {
		val affectedRows = st.executeUpdate
		if (affectedRows == 0)
			throw DaoException(exceptionMessage)
	}

	@throws[SQLException]
	private def tryRetrieveId(st: PreparedStatement,
														exceptionMessageNoRows: String) =
		withResources(st.getGeneratedKeys) {
			generatedKeys: ResultSet => {
				if (generatedKeys.next)
					generatedKeys.getLong(1)
				else
					throw DaoException(exceptionMessageNoRows)
			}
		}

	private def getBirthdayFromUser(user: User): sql.Date =
		user.birthday match {
			case Some(date) => new sql.Date(date.getTime)
			case None => null
		}

	override def update(entity: User) =
		throw new UnsupportedOperationException
}
