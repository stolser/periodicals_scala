package com.stolser.javatraining.webproject.controller.security

import com.stolser.javatraining.webproject.FunSuiteFunctionBase
import com.stolser.javatraining.webproject.controller.ApplicationResources.{CURRENT_USER_ATTR_NAME, LOGIN_PAGE, ORIGINAL_URI_ATTR_NAME}
import com.stolser.javatraining.webproject.TestResources
import com.stolser.javatraining.webproject.controller.request.processor.RequestProcessor
import com.stolser.javatraining.webproject.controller.security.AuthenticationFilterTest._
import com.stolser.javatraining.webproject.model.entity.user.User
import com.sun.java.swing.plaf.windows.resources.windows_zh_HK
import javax.servlet.FilterChain
import javax.servlet.http.{HttpServletRequest, HttpServletResponse, HttpSession}

/**
  * Created by Oleg Stoliarov on 10/21/18.
  */
object AuthenticationFilterTest {
	private val USER_ID = 77
}

class AuthenticationFilterTest extends FunSuiteFunctionBase {
	private val session = mock[HttpSession]
	private val request = mock[HttpServletRequest]
	private val response = mock[HttpServletResponse]
	private val chain = mock[FilterChain]

	test("doFilter() if user in session is null") {
		(session.getAttribute _).expects(CURRENT_USER_ATTR_NAME).returning(User(USER_ID))

		val requestURI = TestResources.USER_2_INVOICE_10_PAYMENT
		(request.getRequestURI _).expects().returning(requestURI)

//		(session.setAttribute _).expects(ORIGINAL_URI_ATTR_NAME, requestURI)
		(response.sendRedirect _).expects(LOGIN_PAGE).once()

		new AuthenticationFilter().doFilter(request, response, chain)
	}

}