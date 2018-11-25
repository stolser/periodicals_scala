package com.ostoliarov.webproject.dao.impl.mysql

import java.sql.{Connection, PreparedStatement, ResultSet}

import com.ostoliarov.webproject._
import com.ostoliarov.webproject.dao.RoleDao
import com.ostoliarov.webproject.dao.impl.mysql.MysqlRoleDao._
import com.ostoliarov.webproject.model.entity.user.UserRole
import com.ostoliarov.webproject.model.entity.user.UserRole.UserRole

import scala.collection.mutable

/**
	* Created by Oleg Stoliarov on 10/14/18.
	*/

object MysqlRoleDao {
	private val DB_USER_ROLES_NAME = "user_roles.name"
	private val RETRIEVING_ROLES_FOR_USER = "Exception during retrieving roles for user with userName = '%s'"
	private val EXCEPTION_DURING_INSERTING_ROLE = "Exception during executing statement: '%s' for " + "userId = %d"
}

class MysqlRoleDao private[mysql](conn: Connection) extends RoleDao {

	override def findRolesByUserName(userName: String): Set[UserRole] = {
		val sqlStatement = "SELECT user_roles.name " +
			"FROM users INNER JOIN user_roles " +
			"ON (users.id = user_roles.user_id) " +
			"INNER JOIN credentials " +
			"ON (credentials.user_id = users.id) " +
			"WHERE credentials.user_name = ?"

		tryAndCatchSqlException(exceptionMessage = RETRIEVING_ROLES_FOR_USER.format(userName)) {
			withResources(conn.prepareStatement(sqlStatement)) {
				st: PreparedStatement => {
					st.setString(1, userName)

					withResources(st.executeQuery()) {
						rs: ResultSet =>
							val roles = mutable.Set[UserRole]()
							while (rs.next)
								roles.add(UserRole.withName(rs.getString(DB_USER_ROLES_NAME).toUpperCase))

							roles.toSet
					}
				}
			}
		}
	}

	override def addRole(userId: Long, role: UserRole): Unit = {
		val sqlStatement = "INSERT INTO user_roles (user_id, name) VALUES (?, ?)"
		val exceptionMessage = EXCEPTION_DURING_INSERTING_ROLE.format(sqlStatement, userId)

		tryAndCatchSqlException(exceptionMessage) {
			withResources(conn.prepareStatement(sqlStatement)) {
				st: PreparedStatement => {
					st.setLong(1, userId)
					st.setString(2, role.toString.toLowerCase())

					st.executeUpdate
				}
			}
		}
	}
}
