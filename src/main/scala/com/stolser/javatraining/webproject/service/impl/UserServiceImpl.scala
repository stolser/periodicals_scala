package com.stolser.javatraining.webproject.service.impl

import com.stolser.javatraining.webproject.connection.pool.{ConnectionPool, ConnectionPoolProvider}
import com.stolser.javatraining.webproject.dao.{AbstractConnection, DaoFactory}
import com.stolser.javatraining.webproject.model.entity.user.{Credential, User, UserRole}
import com.stolser.javatraining.webproject.service.ServiceUtils.withConnection
import com.stolser.javatraining.webproject.service.UserService

/**
	* Created by Oleg Stoliarov on 10/15/18.
	*/
object UserServiceImpl extends UserService {
	private lazy val factory = DaoFactory.mysqlDaoFactory
	private implicit lazy val connectionPool: ConnectionPool = ConnectionPoolProvider.getPool

	private object UserParameterName extends Enumeration {
		val ID, NAME = Value
	}

	private def findOneUserBy(paramName: UserParameterName.Value,
														paramValue: Any): Option[User] = {
		withConnection { conn =>
			val userInDb = paramName match {
				case UserParameterName.ID => factory.userDao(conn)
					.findOneById(paramValue.asInstanceOf[Long])
				case UserParameterName.NAME => factory.userDao(conn)
					.findOneByUserName(paramValue.asInstanceOf[String])
			}

			userInDb match {
				case Some(user) =>
					setUserRoles(user, conn)
					Some(user)
				case _ => None
			}
		}
	}

	override def findOneById(id: Long): Option[User] =
		findOneUserBy(UserParameterName.ID, id)

	override def findOneByName(userName: String): Option[User] =
		findOneUserBy(UserParameterName.NAME, userName)

	private def setUserRoles(user: User,
													 conn: AbstractConnection): Unit =
		user.roles = factory.roleDao(conn).findRolesByUserName(user.userName)

	override def findOneCredentialByUserName(userName: String): Option[Credential] =
		withConnection { conn =>
			factory.credentialDao(conn)
				.findCredentialByUserName(userName)
		}

	override def findAll: List[User] =
		withConnection { conn =>
			val roleDao = factory.roleDao(conn)

			for (user <- factory.userDao(conn).findAll)
				yield {
					user.roles = roleDao.findRolesByUserName(user.userName)
					user
				}
		}

	override def createNewUser(user: User,
														 credential: Credential,
														 userRole: UserRole.Value): Boolean = {
		require(user != null)
		require(credential != null)
		require(userRole != null)

		withConnection { conn =>
			conn.beginTransaction()

			val userId = factory.userDao(conn).createNew(user)
			credential.userId = userId
			val isNewCredentialCreated = factory.credentialDao(conn)
				.createNew(credential)

			if (!isNewCredentialCreated) {
				conn.rollbackTransaction()
				return false
			}

			factory.roleDao(conn).addRole(userId, userRole)
			conn.commitTransaction()

			return true
		}
	}

	override def emailExistsInDb(email: String): Boolean =
		withConnection { conn =>
			factory.userDao(conn).emailExistsInDb(email)
		}
}
