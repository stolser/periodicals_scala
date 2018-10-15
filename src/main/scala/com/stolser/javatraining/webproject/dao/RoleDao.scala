package com.stolser.javatraining.webproject.dao

import java.util
import java.util.Set

import com.stolser.javatraining.webproject.model.entity.user.User

/**
  * Created by Oleg Stoliarov on 10/13/18.
  */
trait RoleDao {
	/**
	  * Retrieves all the roles that a user with the specified username has.
	  */
	def findRolesByUserName(userName: String): util.Set[User.Role]

	def addRole(userId: Long, role: User.Role): Unit
}
