package com.ostoliarov.webproject.controller.request.processor.sign

import com.ostoliarov.webproject.controller.ApplicationResources.SIGN_UP_PAGE_VIEW_NAME
import com.ostoliarov.webproject.controller.request.processor.DispatchType.FORWARD
import com.ostoliarov.webproject.controller.request.processor.{AbstractViewName, RequestProcessor, ResourceRequest}
import com.ostoliarov.webproject.model.entity.user.UserRole
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import scala.collection.JavaConverters._

/**
	* Created by Oleg Stoliarov on 10/11/18.
	*/
object DisplaySignUpPage extends RequestProcessor {
	override def process(request: HttpServletRequest,
											 response: HttpServletResponse): ResourceRequest = {
		request.setAttribute("roles", UserRole.values.asJava)

		ResourceRequest(FORWARD, AbstractViewName(SIGN_UP_PAGE_VIEW_NAME))
	}
}
