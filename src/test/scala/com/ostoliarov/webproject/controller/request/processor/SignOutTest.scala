package com.ostoliarov.webproject.controller.request.processor

import com.ostoliarov.webproject.FunSuiteWithMockitoScalaBase
import com.ostoliarov.webproject.controller.ApplicationResources.CURRENT_USER_ATTR_NAME
import com.ostoliarov.webproject.controller.request.processor.sign._
import com.ostoliarov.webproject.model.entity.user.User
import javax.servlet.http.{HttpServletRequest, HttpServletResponse, HttpSession}

/**
	* Created by Oleg Stoliarov on 11/6/18.
	*/
class SignOutTest extends FunSuiteWithMockitoScalaBase {
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
		when(sessionMock.getAttribute(any[String])) thenReturn User()

		SignOut.process(requestMock, responseMock)

		verify(sessionMock) removeAttribute CURRENT_USER_ATTR_NAME
		verify(sessionMock) invalidate()
	}
}
