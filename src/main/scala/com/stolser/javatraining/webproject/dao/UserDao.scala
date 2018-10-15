package com.stolser.javatraining.webproject.dao

import com.stolser.javatraining.webproject.model.entity.user.User

/**
  * Created by Oleg Stoliarov on 10/13/18.
  */
trait UserDao extends GenericDao[User] {
	def findOneByUserName(userName: String): User

	def emailExistsInDb(email: String): Boolean
}
