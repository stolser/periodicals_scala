package com.ostoliarov.webproject.service.impl

import com.ostoliarov.webproject.connection.AbstractConnection
import com.ostoliarov.webproject.model.entity.user.UserRole.UserRole
import com.ostoliarov.webproject.model.entity.user.{Credential, User}
import com.ostoliarov.webproject.service.{UserService, _}

/**
	* Created by Oleg Stoliarov on 11/21/18.
	*/

abstract class UserServiceImpl extends UserService {
	this: ServiceDependency =>

	private object UserParameterName extends Enumeration {
		type UserParameterName = Value
		val ID, NAME = Value
	}

	import UserParameterName._

	override def findOneById(id: Long): Option[User] =
		findOneUserBy(UserParameterName.ID, id)

	override def findOneByName(userName: String): Option[User] =
		findOneUserBy(UserParameterName.NAME, userName)

	private def findOneUserBy(paramName: UserParameterName,
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
														 userRole: UserRole): Option[User] = {
		require(user != null)
		require(credential != null)
		require(userRole != null)

		withConnection { conn =>
			conn.beginTransaction()

			val newUserId = daoFactory.userDao(conn).createNew(user)
			credential.userId = newUserId

			val isNewCredentialCreated = daoFactory.credentialDao(conn).createNew(credential)

			if (!isNewCredentialCreated) {
				conn.rollbackTransaction()
				return None
			}

			daoFactory.roleDao(conn).addRole(newUserId, userRole)
			conn.commitTransaction()

			return Some(user.copy(id = newUserId, roles = Set(userRole)))
		}
	}

	override def emailExistsInDb(email: String): Boolean =
		withConnection { conn =>
			daoFactory.userDao(conn).emailExistsInDb(email)
		}
}
