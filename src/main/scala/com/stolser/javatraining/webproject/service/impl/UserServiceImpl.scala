package com.stolser.javatraining.webproject.service.impl

import java.util
import java.util.Objects.nonNull

import com.stolser.javatraining.webproject.connection.pool.{ConnectionPool, ConnectionPoolProvider}
import com.stolser.javatraining.webproject.dao.{AbstractConnection, DaoFactory}
import com.stolser.javatraining.webproject.model.entity.user.{Credential, User, UserRole}
import com.stolser.javatraining.webproject.service.ServiceUtils.withConnection
import com.stolser.javatraining.webproject.service.UserService

import scala.collection.JavaConverters._

/**
  * Created by Oleg Stoliarov on 10/15/18.
  */
object UserServiceImpl extends UserService {
	private lazy val factory = DaoFactory.getMysqlDaoFactory
	private implicit lazy val connectionPool: ConnectionPool = ConnectionPoolProvider.getPool

	override def findOneById(id: Long): User =
		withConnection { conn =>
			val user = factory.getUserDao(conn)
				.findOneById(id)
			setUserRoles(user, conn)

			user
		}

	private def setUserRoles(user: User,
							 conn: AbstractConnection): Unit =
		if (nonNull(user)) {
			user.roles = factory.getRoleDao(conn)
				.findRolesByUserName(user.userName).asScala.toSet
		}

	override def findOneCredentialByUserName(userName: String): Credential =
		withConnection { conn =>
			factory.getCredentialDao(conn)
				.findCredentialByUserName(userName)
		}

	override def findOneUserByUserName(userName: String): User =
		withConnection { conn =>
			val user = factory.getUserDao(conn)
				.findOneByUserName(userName)
			setUserRoles(user, conn)

			user
		}

	override def findAll: util.List[User] =
		withConnection { conn =>
			val allUser = factory.getUserDao(conn).findAll

			allUser.forEach(user => {
				user.roles = factory.getRoleDao(conn)
					.findRolesByUserName(user.userName).asScala.toSet
			})

			allUser
		}

	override def createNewUser(user: User,
							   credential: Credential,
							   userRole: UserRole.Value): Boolean =
		withConnection { conn =>
			conn.beginTransaction()

			val userId = factory.getUserDao(conn).createNew(user)
			credential.userId = userId
			val isNewCredentialCreated = factory.getCredentialDao(conn)
				.createNew(credential)

			if (!isNewCredentialCreated) {
				conn.rollbackTransaction()
				return false
			}

			factory.getRoleDao(conn).addRole(userId, userRole)
			conn.commitTransaction()

			return true
		}

	override def emailExistsInDb(email: String): Boolean =
		withConnection { conn =>
			factory.getUserDao(conn).emailExistsInDb(email)
		}
}
