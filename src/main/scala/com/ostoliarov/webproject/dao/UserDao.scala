package com.ostoliarov.webproject.dao

import com.ostoliarov.webproject.model.entity.user.User

/**
	* Created by Oleg Stoliarov on 10/13/18.
	*/
trait UserDao extends GenericDao[User] {
	def findOneByUserName(userName: String): Option[User]

	def emailExistsInDb(email: String): Boolean
}
