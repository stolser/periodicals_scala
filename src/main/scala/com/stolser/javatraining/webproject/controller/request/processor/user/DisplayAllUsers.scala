package com.stolser.javatraining.webproject.controller.request.processor.user

import com.stolser.javatraining.webproject.controller.ApplicationResources.{ALL_USERS_ATTR_NAME, USER_LIST_VIEW_NAME}
import com.stolser.javatraining.webproject.controller.request.processor.RequestProcessor
import com.stolser.javatraining.webproject.service.impl.mysql.UserServiceMysqlImpl
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import scala.collection.JavaConverters._

/**
	* Created by Oleg Stoliarov on 10/11/18.
	* Processes a GET request to a page with a list of all users in the system.
	*/
object DisplayAllUsers extends RequestProcessor {
	private val userService = UserServiceMysqlImpl

	override def process(request: HttpServletRequest,
											 response: HttpServletResponse): String = {
		request.setAttribute(ALL_USERS_ATTR_NAME, userService.findAll.asJava)

		FORWARD + USER_LIST_VIEW_NAME
	}
}
