package com.ostoliarov.webproject.controller.request.processor.sign

import com.ostoliarov.webproject.controller.ApplicationResources.{CURRENT_USER_ATTR_NAME, LOGIN_PAGE}
import com.ostoliarov.webproject.controller.request.processor.{AbstractViewName, RequestProcessor, ResourceRequest}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import com.ostoliarov.webproject.controller.request.processor.DispatchType.REDIRECT

/**
  * Created by Oleg Stoliarov on 10/11/18.
  * Processes a GET request to sing out the current user and redirects to a 'login' page.
  */
object SignOut extends RequestProcessor {
	override def process(request: HttpServletRequest,
											 response: HttpServletResponse): ResourceRequest = {
		request.getSession.removeAttribute(CURRENT_USER_ATTR_NAME)
		request.getSession.invalidate()

		ResourceRequest(REDIRECT, AbstractViewName(LOGIN_PAGE))
	}
}
