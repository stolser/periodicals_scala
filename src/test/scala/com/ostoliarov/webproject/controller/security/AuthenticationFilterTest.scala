package com.ostoliarov.webproject.controller.security

import com.ostoliarov.webproject.controller.ApplicationResources._
import com.ostoliarov.webproject.controller.security.AuthenticationFilterTest._
import com.ostoliarov.webproject.controller.utils.HttpUtilsTrait
import com.ostoliarov.webproject.model.entity.user.User
import com.ostoliarov.webproject.model.entity.user.UserStatus._
import com.ostoliarov.webproject.{FunSuiteWithMockitoScalaBase, TestResources}
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

class AuthenticationFilterTest extends FunSuiteWithMockitoScalaBase {
	private var sessionMock: HttpSession = _
	private var requestMock: HttpServletRequest = _
	private var responseMock: HttpServletResponse = _
	private var filterChainMock: FilterChain = _
	private var httpUtilsMock: HttpUtilsTrait = _

	before {
		sessionMock = mock[HttpSession]
		requestMock = mock[HttpServletRequest]
		responseMock = mock[HttpServletResponse]
		filterChainMock = mock[FilterChain]
		httpUtilsMock = mock[HttpUtilsTrait]
	}

	private def authenticationFilterWithMocks =
		new AuthenticationFilter() {
			override private[security] val httpUtils: HttpUtilsTrait = httpUtilsMock
		}

	test("doFilter() Should call doFilter() when request does Not require authentication") {
		val requestURI = SIGN_IN_URI

		when(requestMock.getRequestURI) thenReturn requestURI

		new AuthenticationFilter().doFilter(requestMock, responseMock, filterChainMock)

		verify(filterChainMock).doFilter(requestMock, responseMock)
		verify(sessionMock, times(0)).setAttribute(anyString(), anyString())
		verify(responseMock, times(0)).sendRedirect(anyString())
	}

	test("doFilter() Should redirect to LOGIN_PAGE when the user in session is null") {
		val requestURI = TestResources.USER_2_INVOICE_10_PAYMENT_URI

		when(sessionMock.getAttribute(CURRENT_USER_ATTR_NAME)) thenReturn User(USER_ID)
		when(requestMock.getSession) thenReturn sessionMock
		when(requestMock.getRequestURI) thenReturn requestURI

		new AuthenticationFilter().doFilter(requestMock, responseMock, filterChainMock)

		verify(sessionMock).setAttribute(ORIGINAL_URI_ATTR_NAME, requestURI)
		verify(responseMock).sendRedirect(LOGIN_PAGE)
	}

	test("doFilter() Should redirect to SIGN_OUT_URI when the user from session is 'BLOCKED'") {
		val requestURI = TestResources.USER_2_INVOICE_10_PAYMENT_URI

		when(requestMock.getRequestURI) thenReturn requestURI
		when(httpUtilsMock.currentUserFromFromDb(requestMock)) thenReturn Some(User(id = 2, status = BLOCKED))

		authenticationFilterWithMocks.doFilter(requestMock, responseMock, filterChainMock)

		verify(responseMock).sendRedirect(SIGN_OUT_URI)
		verify(filterChainMock, times(0)).doFilter(any[HttpServletRequest], any[HttpServletResponse])
		verify(sessionMock, times(0)).setAttribute(anyString(), anyString())
	}

	test("doFilter() Should call doFilter() when the user from session is 'ACTIVE'") {
		val requestURI = TestResources.USER_2_INVOICE_10_PAYMENT_URI

		when(requestMock.getRequestURI) thenReturn requestURI
		when(httpUtilsMock.currentUserFromFromDb(ArgumentMatchers.any[HttpServletRequest]))
			.thenReturn(Some(User(id = 2, status = ACTIVE)))

		authenticationFilterWithMocks.doFilter(requestMock, responseMock, filterChainMock)

		verify(filterChainMock).doFilter(requestMock, responseMock)
		verify(responseMock, times(0)).sendRedirect(anyString())
		verify(sessionMock, times(0)).setAttribute(anyString(), anyString())
	}
}