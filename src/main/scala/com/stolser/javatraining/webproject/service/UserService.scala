package com.stolser.javatraining.webproject.service

import com.stolser.javatraining.webproject.model.entity.user.{Credential, User, UserRole}

/**
	* Created by Oleg Stoliarov on 10/15/18.
	*/
trait UserService {
	def findOneById(id: Long): User

	def findOneCredentialByUserName(userName: String): Credential

	def findOneUserByUserName(userName: String): User

	def findAll: List[User]

	def createNewUser(user: User,
										credential: Credential,
										userRole: UserRole.Value): Boolean

	def emailExistsInDb(email: String): Boolean
}
