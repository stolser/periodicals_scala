package com.stolser.javatraining.webproject.dao.impl.mysql

import java.sql._
import java.{sql, util}
import java.util.Objects.nonNull
import java.util.{ArrayList, Date, List}

import com.stolser.javatraining.webproject.dao.DaoUtils._
import com.stolser.javatraining.webproject.utils.TryWithResources.withResources
import com.stolser.javatraining.webproject.dao.exception.DaoException
import com.stolser.javatraining.webproject.dao.{DaoUtils, UserDao}
import com.stolser.javatraining.webproject.model.entity.user.{User, UserStatus}

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
}

class MysqlUserDao(conn: Connection) extends UserDao {

	import MysqlUserDao._

	override def findOneById(id: Long): User = {
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
								getUserFromResults(rs)
							else
								null
					}
				}
			}
		}
	}

	override def findOneByUserName(userName: String): User = {
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
								getUserFromResults(rs)
							else
								null
					}
				}
			}
		}
	}

	override def emailExistsInDb(email: String): Boolean = {
		val sqlStatement = "SELECT COUNT(id) FROM users " +
			"WHERE users.email = ?"

		tryAndCatchSqlException() { () =>
			withResources(conn.prepareStatement(sqlStatement)) {
				st: PreparedStatement => {
					st.setString(1, email)

					withResources(st.executeQuery()) {
						rs: ResultSet =>
							rs.next && rs.getInt(1) > 0
					}
				}
			}
		}
	}

	override def findAll: util.List[User] = {
		val sqlStatement = "SELECT * FROM credentials " +
			"RIGHT OUTER JOIN users ON (credentials.user_id = users.id)"

		tryAndCatchSqlException(exceptionMessage = EXCEPTION_DURING_FINDING_ALL_USERS) { () =>
			withResources(conn.prepareStatement(sqlStatement)) {
				st: PreparedStatement => {
					withResources(st.executeQuery()) {
						rs: ResultSet =>
							val users = new util.ArrayList[User]

							while (rs.next)
								users.add(getUserFromResults(rs))

							users
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
			firstName = rs.getString(DB_USERS_FIRST_NAME),
			lastName = rs.getString(DB_USERS_LAST_NAME),
			birthday = getBirthdayFromRs(rs),
			email = rs.getString(DB_USERS_EMAIL),
			address = rs.getString(DB_USERS_ADDRESS),
			status = UserStatus.withName(rs.getString(DB_USERS_STATUS).toUpperCase)
		)

	@throws[SQLException]
	private def getBirthdayFromRs(rs: ResultSet) = {
		val birthday = rs.getDate(DB_USERS_BIRTHDAY)

		if (nonNull(birthday))
			new Date(birthday.getTime)
		else
			null
	}

	override def createNew(user: User): Long = {
		val exceptionMessage = EXCEPTION_DURING_CREATING_NEW_USER.format(user)
		val exceptionMessageNoRows = String.format(CREATING_USER_FAILED_NO_ROWS_AFFECTED, user)
		val sqlStatement = "INSERT INTO users " +
			"(first_name, last_name, birthday, email, address, status) " +
			"VALUES (?, ?, ?, ?, ?, ?)"

		tryAndCatchSqlException(exceptionMessage) { () =>
			withResources(conn.prepareStatement(sqlStatement, Statement.RETURN_GENERATED_KEYS)) {
				st: PreparedStatement => {
					st.setString(1, user.getFirstName)
					st.setString(2, user.getLastName)
					st.setDate(3, getBirthdayFromUser(user))
					st.setString(4, user.getEmail)
					st.setString(5, user.getAddress)
					st.setString(6, user.getStatus.toString.toLowerCase)

					tryExecuteUpdate(st, exceptionMessage)

					tryRetrieveId(st, exceptionMessageNoRows)
				}
			}
		}
	}

	@throws[SQLException]
	private def tryExecuteUpdate(st: PreparedStatement,
								 exceptionMessage: String): Unit = {
		val affectedRows = st.executeUpdate
		if (affectedRows == 0)
			throw new DaoException(exceptionMessage)
	}

	@throws[SQLException]
	private def tryRetrieveId(st: PreparedStatement,
							  exceptionMessageNoRows: String) =
		withResources(st.getGeneratedKeys) {
			generatedKeys: ResultSet => {
				if (generatedKeys.next)
					generatedKeys.getLong(1)
				else
					throw new DaoException(exceptionMessageNoRows)
			}
		}

	private def getBirthdayFromUser(user: User): sql.Date = {
		val birthday: java.util.Date = user.getBirthday
		if (nonNull(birthday))
			new sql.Date(birthday.getTime)
		else
			null
	}

	override def update(entity: User) =
		throw new UnsupportedOperationException
}
