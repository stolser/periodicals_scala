package com.stolser.javatraining.webproject.service.impl

import java.util.Objects.nonNull

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

	override def findOneById(id: Long): User =
		withConnection { conn =>
			val user = factory.userDao(conn)
				.findOneById(id)
			setUserRoles(user, conn)

			user
		}

	private def setUserRoles(user: User,
													 conn: AbstractConnection): Unit =
		if (nonNull(user)) {
			user.roles = factory.roleDao(conn)
				.findRolesByUserName(user.userName)
		}

	override def findOneCredentialByUserName(userName: String): Credential =
		withConnection { conn =>
			factory.credentialDao(conn)
				.findCredentialByUserName(userName)
		}

	override def findOneUserByUserName(userName: String): User =
		withConnection { conn =>
			val user = factory.userDao(conn)
				.findOneByUserName(userName)
			setUserRoles(user, conn)

			user
		}

	override def findAll: List[User] =
		withConnection { conn =>
			val allUser = factory.userDao(conn).findAll

			allUser.foreach(user => {
				user.roles = factory.roleDao(conn)
					.findRolesByUserName(user.userName)
			})

			allUser
		}

	override def createNewUser(user: User,
														 credential: Credential,
														 userRole: UserRole.Value): Boolean =
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

	override def emailExistsInDb(email: String): Boolean =
		withConnection { conn =>
			factory.userDao(conn).emailExistsInDb(email)
		}
}
