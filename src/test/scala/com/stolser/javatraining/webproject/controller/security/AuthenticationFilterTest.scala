package com.stolser.javatraining.webproject.controller.security

import com.stolser.javatraining.webproject.controller.ApplicationResources._
import com.stolser.javatraining.webproject.controller.security.AuthenticationFilterTest._
import com.stolser.javatraining.webproject.controller.utils.HttpUtilsTrait
import com.stolser.javatraining.webproject.model.entity.user.User
import com.stolser.javatraining.webproject.model.entity.user.UserStatus._
import com.stolser.javatraining.webproject.{FunSuiteBase, TestResources}
import javax.servlet.FilterChain
import javax.servlet.http.{HttpServletRequest, HttpServletResponse, HttpSession}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers._

/**
	* Created by Oleg Stoliarov on 10/21/18.
	*/
object AuthenticationFilterTest {
	private val USER_ID = 77
}

class AuthenticationFilterTest extends FunSuiteBase {
	private var session: HttpSession = _
	private var request: HttpServletRequest = _
	private var response: HttpServletResponse = _
	private var chain: FilterChain = _
	private var httpUtilsMock: HttpUtilsTrait = _

	before {
		session = mock[HttpSession]
		request = mock[HttpServletRequest]
		response = mock[HttpServletResponse]
		chain = mock[FilterChain]
		httpUtilsMock = mock[HttpUtilsTrait]
	}

	private def getAuthenticationFilterWithMocks =
		new AuthenticationFilter() {
			override private[security] val httpUtils: HttpUtilsTrait = httpUtilsMock
		}

	test("doFilter() Should call doFilter() when request does Not require authentication") {
		val requestURI = SIGN_IN_URI

		when(request.getRequestURI) thenReturn requestURI

		new AuthenticationFilter().doFilter(request, response, chain)

		verify(chain).doFilter(request, response)
		verify(session, times(0)).setAttribute(anyString(), anyString())
		verify(response, times(0)).sendRedirect(anyString())
	}

	test("doFilter() Should redirect to LOGIN_PAGE when the user in session is null") {
		val requestURI = TestResources.USER_2_INVOICE_10_PAYMENT_URI

		when(session.getAttribute(CURRENT_USER_ATTR_NAME)) thenReturn User(USER_ID)
		when(request.getSession) thenReturn session
		when(request.getRequestURI) thenReturn requestURI

		new AuthenticationFilter().doFilter(request, response, chain)

		verify(session).setAttribute(ORIGINAL_URI_ATTR_NAME, requestURI)
		verify(response).sendRedirect(LOGIN_PAGE)
	}

	test("doFilter() Should redirect to SIGN_OUT_URI when the user from session is 'BLOCKED'") {
		val requestURI = TestResources.USER_2_INVOICE_10_PAYMENT_URI

		when(request.getRequestURI) thenReturn requestURI
		when(httpUtilsMock.currentUserFromFromDb(request)) thenReturn Some(User(id = 2, status = BLOCKED))

		getAuthenticationFilterWithMocks.doFilter(request, response, chain)

		verify(response).sendRedirect(SIGN_OUT_URI)
		verify(chain, times(0)).doFilter(any[HttpServletRequest], any[HttpServletResponse])
		verify(session, times(0)).setAttribute(anyString(), anyString())
	}

	test("doFilter() Should call doFilter() when the user from session is 'ACTIVE'") {
		val requestURI = TestResources.USER_2_INVOICE_10_PAYMENT_URI

		when(request.getRequestURI) thenReturn requestURI
		when(httpUtilsMock.currentUserFromFromDb(ArgumentMatchers.any[HttpServletRequest]))
			.thenReturn(Some(User(id = 2, status = ACTIVE)))

		getAuthenticationFilterWithMocks.doFilter(request, response, chain)

		verify(chain).doFilter(request, response)
		verify(response, times(0)).sendRedirect(anyString())
		verify(session, times(0)).setAttribute(anyString(), anyString())
	}
}