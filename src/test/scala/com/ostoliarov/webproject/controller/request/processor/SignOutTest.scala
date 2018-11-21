package com.ostoliarov.webproject.controller.request.processor

import com.ostoliarov.webproject.FunSuiteMockitoScalaBase
import com.ostoliarov.webproject.controller.ApplicationResources.CURRENT_USER_ATTR_NAME
import com.ostoliarov.webproject.controller.request.processor.sign._
import javax.servlet.http.{HttpServletRequest, HttpServletResponse, HttpSession}

/**
	* Created by Oleg Stoliarov on 11/6/18.
	*/
class SignOutTest extends FunSuiteMockitoScalaBase {
	private var sessionMock: HttpSession = _
	private var requestMock: HttpServletRequest = _
	private var responseMock: HttpServletResponse = _

	before {
		sessionMock = mock[HttpSession]
		requestMock = mock[HttpServletRequest]
		responseMock = mock[HttpServletResponse]
	}

	test("SignOut.process() Should invalidate current session") {
		when(requestMock.getSession) thenReturn sessionMock

		SignOut.process(requestMock, responseMock)

		verify(sessionMock) removeAttribute CURRENT_USER_ATTR_NAME
		verify(sessionMock) invalidate()
	}
}
