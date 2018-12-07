package com.ostoliarov.webproject.controller.request.processor.sign

import com.ostoliarov.eventsourcing.logging.EventLoggingHelper
import com.ostoliarov.eventsourcing.logging.model.SignOutEvent
import com.ostoliarov.webproject.controller.ApplicationResources.{CURRENT_USER_ATTR_NAME, LOGIN_PAGE}
import com.ostoliarov.webproject.controller.request.processor.DispatchType.REDIRECT
import com.ostoliarov.webproject.controller.request.processor.{AbstractViewName, RequestProcessor, ResourceRequest}
import com.ostoliarov.webproject.model.entity.user.User
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

/**
	* Created by Oleg Stoliarov on 10/11/18.
	* Processes a GET request to sing out the current user and redirects to a 'login' page.
	*/
object SignOut extends RequestProcessor {
	override def process(request: HttpServletRequest,
											 response: HttpServletResponse): ResourceRequest = {
		val userToSignOut = request.getSession.getAttribute(CURRENT_USER_ATTR_NAME).asInstanceOf[User]

		request.getSession.removeAttribute(CURRENT_USER_ATTR_NAME)
		request.getSession.invalidate()

		EventLoggingHelper.logEvent(SignOutEvent(userToSignOut.id))

		ResourceRequest(REDIRECT, AbstractViewName(LOGIN_PAGE))
	}
}
