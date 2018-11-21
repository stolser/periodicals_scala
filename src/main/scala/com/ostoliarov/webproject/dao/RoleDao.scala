package com.ostoliarov.webproject.dao

import com.ostoliarov.webproject.model.entity.user.UserRole

/**
	* Created by Oleg Stoliarov on 10/13/18.
	*/
trait RoleDao {
	/**
		* Retrieves all the roles that a user with the specified username has.
		*/
	def findRolesByUserName(userName: String): Set[UserRole.Value]

	def addRole(userId: Long, role: UserRole.Value): Unit
}
