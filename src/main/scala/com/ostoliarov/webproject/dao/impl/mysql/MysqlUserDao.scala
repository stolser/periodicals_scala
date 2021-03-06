package com.ostoliarov.webproject.dao.impl.mysql

import java.sql.{Date => SqlDate, _}
import java.util.{Date => JavaDate}

import com.ostoliarov.webproject._
import com.ostoliarov.webproject.dao.impl.mysql.MysqlUserDao._
import com.ostoliarov.webproject.dao.{UserDao, _}
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
	private val SELECT_COUNT_FROM_USERS_WHERE_EMAIL = "SELECT COUNT(id) FROM users WHERE users.email = ?"
}

class MysqlUserDao private[mysql](conn: Connection) extends UserDao {

	override def findOneById(id: Long): Option[User] = {
		val sqlStatement = "SELECT * FROM users " +
			"JOIN credentials ON (users.id = credentials.user_id) " +
			"WHERE users.id = ?"

		tryAndCatchSqlException(exceptionMessage = EXCEPTION_DURING_FINDING_USER_BY_ID.format(id)) {
			withResources(conn.prepareStatement(sqlStatement)) {
				st: PreparedStatement => {
					st.setLong(1, id)

					withResources(st.executeQuery()) {
						rs: ResultSet =>
							if (rs.next)
								Some(getUserFromRs(rs))
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

		tryAndCatchSqlException(exceptionMessage = EXCEPTION_DURING_FINDING_USER_BY_NAME.format(userName)) {
			withResources(conn.prepareStatement(sqlStatement)) {
				st: PreparedStatement => {
					st.setString(1, userName)

					withResources(st.executeQuery()) {
						rs: ResultSet =>
							if (rs.next)
								Some(getUserFromRs(rs))
							else None
					}
				}
			}
		}
	}

	override def emailExistsInDb(email: String): Boolean =
		tryAndCatchSqlException() {
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

		tryAndCatchSqlException(exceptionMessage = EXCEPTION_DURING_FINDING_ALL_USERS) {
			withResources(conn.prepareStatement(sqlStatement)) {
				st: PreparedStatement => {
					withResources(st.executeQuery()) {
						rs: ResultSet =>
							val users = mutable.Buffer[User]()

							while (rs.next)
								users += getUserFromRs(rs)

							users.toList
					}
				}
			}
		}
	}

	@throws[SQLException]
	private def getUserFromRs(rs: ResultSet) =
		User(
			id = rs.getLong(DB_USERS_ID),
			userName = rs.getString(MysqlCredentialDao.DB_CREDENTIALS_USER_NAME),
			firstName = Option(rs.getString(DB_USERS_FIRST_NAME)),
			lastName = Option(rs.getString(DB_USERS_LAST_NAME)),
			birthday = birthdayFromResultSet(rs),
			email = rs.getString(DB_USERS_EMAIL),
			address = Option(rs.getString(DB_USERS_ADDRESS)),
			status = UserStatus.withName(rs.getString(DB_USERS_STATUS).toUpperCase)
		)

	@throws[SQLException]
	private def birthdayFromResultSet(rs: ResultSet): Option[JavaDate] =
		Option(rs.getDate(DB_USERS_BIRTHDAY)) match {
			case Some(birthday) => Some(new JavaDate(birthday.getTime))
			case None => None
		}

	override def createNew(user: User): Long = {
		val exceptionMessage = EXCEPTION_DURING_CREATING_NEW_USER.format(user)
		val sqlStatement = "INSERT INTO users " +
			"(first_name, last_name, birthday, email, address, status) " +
			"VALUES (?, ?, ?, ?, ?, ?)"

		tryAndCatchSqlException(exceptionMessage) {
			withResources(conn.prepareStatement(sqlStatement, Statement.RETURN_GENERATED_KEYS)) {
				st: PreparedStatement => {
					st.setString(1, user.firstName.getOrElse(""))
					st.setString(2, user.lastName.getOrElse(""))
					st.setDate(3, birthday(user))
					st.setString(4, user.email)
					st.setString(5, user.address.getOrElse(""))
					st.setString(6, user.status.toString.toLowerCase)

					tryCreateNewEntityAndRetrieveGeneratedId(st, exceptionMessage)
				}
			}
		}
	}

	private def birthday(user: User): SqlDate =
		user.birthday match {
			case Some(date) => new SqlDate(date.getTime)
			case None => null
		}

	override def update(entity: User) = throw new UnsupportedOperationException
}
