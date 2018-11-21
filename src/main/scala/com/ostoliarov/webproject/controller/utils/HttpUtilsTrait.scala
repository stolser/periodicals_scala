package com.ostoliarov.webproject.controller.utils

import com.ostoliarov.webproject.model.entity.user.User
import javax.servlet.http.HttpServletRequest

/**
	* Created by Oleg Stoliarov on 11/4/18.
	*/
trait HttpUtilsTrait {
	def currentUserFromFromDb(request: HttpServletRequest): Option[User]
	def firstIdFromUri(uri: String): Int
	def userIdFromSession(request: HttpServletRequest): Long
}
