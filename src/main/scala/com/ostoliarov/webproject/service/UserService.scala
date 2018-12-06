package com.ostoliarov.webproject.service

import com.ostoliarov.webproject.model.entity.user.UserRole.UserRole
import com.ostoliarov.webproject.model.entity.user.{Credential, User}

/**
	* Created by Oleg Stoliarov on 10/15/18.
	*/
trait UserService {
	def findOneById(id: Long): Option[User]

	def findOneCredentialByUserName(userName: String): Option[Credential]

	def findOneByName(userName: String): Option[User]

	def findAll: List[User]

	/**
		*
		* @param user       - must not be null
		* @param credential - must not be null
		* @param userRole   - must not be null
		* @return - true if this user with this credential has been successfully created;
		*         false - otherwise
		* @throws IllegalArgumentException - in case any argument is null
		*/
	@throws[IllegalArgumentException]
	def createNewUser(user: User,
										credential: Credential,
										userRole: UserRole): Option[User]

	def emailExistsInDb(email: String): Boolean
}
