package com.stolser.javatraining.webproject.dao.impl.mysql

import java.sql.{Connection, PreparedStatement, ResultSet}
import java.util
import java.util.Set

import com.stolser.javatraining.webproject.controller.utils.DaoUtils
import com.stolser.javatraining.webproject.dao.DaoUtils.tryAndCatchSqlException
import com.stolser.javatraining.webproject.dao.RoleDao
import com.stolser.javatraining.webproject.utils.TryWithResources.withResources
import com.stolser.javatraining.webproject.model.entity.periodical.Periodical
import com.stolser.javatraining.webproject.model.entity.user.User
import com.stolser.javatraining.webproject.model.entity.user.UserRole

/**
  * Created by Oleg Stoliarov on 10/14/18.
  */

object MysqlRoleDao {
	private val DB_USER_ROLES_NAME = "user_roles.name"
	private val RETRIEVING_ROLES_FOR_USER = "Exception during retrieving roles for user with userName = '%s'"
	private val EXCEPTION_DURING_INSERTING_ROLE = "Exception during executing statement: '%s' for " + "userId = %d"
}

class MysqlRoleDao(conn: Connection) extends RoleDao {

	import MysqlRoleDao._

	override def findRolesByUserName(userName: String): util.Set[UserRole.Value] = {
		val sqlStatement = "SELECT user_roles.name " +
			"FROM users INNER JOIN user_roles " +
			"ON (users.id = user_roles.user_id) " +
			"INNER JOIN credentials " +
			"ON (credentials.user_id = users.id) " +
			"WHERE credentials.user_name = ?"

		tryAndCatchSqlException(exceptionMessage = RETRIEVING_ROLES_FOR_USER.format(userName)) { () =>
			withResources(conn.prepareStatement(sqlStatement)) {
				st: PreparedStatement => {
					st.setString(1, userName)

					withResources(st.executeQuery()) {
						rs: ResultSet =>
							val roles = new util.HashSet[UserRole.Value]()
							while (rs.next)
								roles.add(UserRole.withName(rs.getString(DB_USER_ROLES_NAME).toUpperCase))

							roles
					}
				}
			}
		}
	}

	override def addRole(userId: Long, role: UserRole.Value): Unit = {
		val sqlStatement = "INSERT INTO user_roles (user_id, name) VALUES (?, ?)"
		val exceptionMessage = EXCEPTION_DURING_INSERTING_ROLE.format(sqlStatement, userId)

		tryAndCatchSqlException(exceptionMessage) { () =>
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
