package com.stolser.javatraining.webproject.service.impl

import com.stolser.javatraining.webproject.dao.AbstractConnection
import com.stolser.javatraining.webproject.model.entity.user.{Credential, User, UserRole}
import com.stolser.javatraining.webproject.service.ServiceUtils.withConnection
import com.stolser.javatraining.webproject.service.UserService

/**
	* Created by Oleg Stoliarov on 11/21/18.
	*/
abstract class UserServiceImpl extends UserService {
	this: ServiceDependency =>

	override def findOneById(id: Long): Option[User] =
		findOneUserBy(UserParameterName.ID, id)

	override def findOneByName(userName: String): Option[User] =
		findOneUserBy(UserParameterName.NAME, userName)

	private object UserParameterName extends Enumeration {
		val ID, NAME = Value
	}

	private def findOneUserBy(paramName: UserParameterName.Value,
														paramValue: Any): Option[User] = {
		withConnection { conn =>
			val userInDb = paramName match {
				case UserParameterName.ID => daoFactory.userDao(conn)
					.findOneById(paramValue.asInstanceOf[Long])
				case UserParameterName.NAME => daoFactory.userDao(conn)
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

	private def setUserRoles(user: User,
													 conn: AbstractConnection): Unit =
		user.roles = daoFactory.roleDao(conn).findRolesByUserName(user.userName)

	override def findOneCredentialByUserName(userName: String): Option[Credential] =
		withConnection { conn =>
			daoFactory.credentialDao(conn)
				.findCredentialByUserName(userName)
		}

	override def findAll: List[User] =
		withConnection { conn =>
			val roleDao = daoFactory.roleDao(conn)

			for (user <- daoFactory.userDao(conn).findAll)
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

			val userId = daoFactory.userDao(conn).createNew(user)
			credential.userId = userId
			val isNewCredentialCreated = daoFactory.credentialDao(conn)
				.createNew(credential)

			if (!isNewCredentialCreated) {
				conn.rollbackTransaction()
				return false
			}

			daoFactory.roleDao(conn).addRole(userId, userRole)
			conn.commitTransaction()

			return true
		}
	}

	override def emailExistsInDb(email: String): Boolean =
		withConnection { conn =>
			daoFactory.userDao(conn).emailExistsInDb(email)
		}
}
