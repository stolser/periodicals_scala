package com.stolser.javatraining.webproject.controller.request.processor.sign

import com.stolser.javatraining.webproject.controller.ApplicationResources.SIGN_UP_PAGE_VIEW_NAME
import com.stolser.javatraining.webproject.controller.request.processor.RequestProcessor
import com.stolser.javatraining.webproject.model.entity.user.{User, UserRole}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

/**
  * Created by Oleg Stoliarov on 10/11/18.
  */
object DisplaySignUpPage extends RequestProcessor {
	override def process(request: HttpServletRequest, response: HttpServletResponse): String = {
		import scala.collection.JavaConverters._

		request.setAttribute("roles", UserRole.values.asJava)
		FORWARD + SIGN_UP_PAGE_VIEW_NAME
	}
}
