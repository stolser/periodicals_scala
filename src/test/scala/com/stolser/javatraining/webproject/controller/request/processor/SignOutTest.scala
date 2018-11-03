package com.stolser.javatraining.webproject.controller.request.processor

import com.stolser.javatraining.webproject.FunSuiteBase
import com.stolser.javatraining.webproject.controller.ApplicationResources.CURRENT_USER_ATTR_NAME
import com.stolser.javatraining.webproject.controller.request.processor.sign._
import javax.servlet.http.{HttpServletRequest, HttpServletResponse, HttpSession}

/**
	* Created by Oleg Stoliarov on 11/6/18.
	*/
class SignOutTest extends FunSuiteBase {
	private var session: HttpSession = _
	private var request: HttpServletRequest = _
	private var response: HttpServletResponse = _

	before {
		session = mock[HttpSession]
		request = mock[HttpServletRequest]
		response = mock[HttpServletResponse]
	}

	test("SignOut.process() Should invalidate current session") {
		when(request.getSession) thenReturn session

		SignOut.process(request, response)

		verify(session) removeAttribute CURRENT_USER_ATTR_NAME
		verify(session) invalidate()
	}
}
