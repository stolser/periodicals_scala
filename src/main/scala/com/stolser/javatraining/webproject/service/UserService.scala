package com.stolser.javatraining.webproject.service

import java.util
import java.util.List

import com.stolser.javatraining.webproject.model.entity.user.{Credential, User}

/**
  * Created by Oleg Stoliarov on 10/15/18.
  */
trait UserService {
	def findOneById(id: Long): User

	def findOneCredentialByUserName(userName: String): Credential

	def findOneUserByUserName(userName: String): User

	def findAll: util.List[User]

	def createNewUser(user: User, credential: Credential, userRole: User.Role): Boolean

	def emailExistsInDb(email: String): Boolean
}
