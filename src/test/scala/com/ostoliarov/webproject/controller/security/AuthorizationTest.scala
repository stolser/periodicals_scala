package com.ostoliarov.webproject.controller.security

import com.ostoliarov.webproject.FunSuiteWithMockitoScalaBase
import com.ostoliarov.webproject.TestResources._
import com.ostoliarov.webproject.controller.ApplicationResources._
import com.ostoliarov.webproject.model.entity.user.{User, UserRole}
import javax.servlet.http.{HttpServletRequest, HttpSession}

/**
	* Created by Oleg Stoliarov on 11/5/18.
	*/
class AuthorizationTest extends FunSuiteWithMockitoScalaBase {
	private var sessionMock: HttpSession = _
	private var requestMock: HttpServletRequest = _
	private val admin = User(roles = Set(UserRole.ADMIN))
	private val notAdmin = User(roles = Set(UserRole.SUBSCRIBER))

	before {
		sessionMock = mock[HttpSession]
		requestMock = mock[HttpServletRequest]
		when(requestMock.getSession) thenReturn sessionMock
	}

	test("checkPermissions() when accessing 'persist periodical' Should return true for admin") {
		setupMocks(
			httpMethod = "post",
			requestUri = PERIODICAL_LIST_URI,
			currentUser = admin
		)

		assertPermissionsAreGranted
	}

	test("checkPermissions() when accessing 'Persist a Periodical' Should return false for Not admin") {
		setupMocks(
			httpMethod = "post",
			requestUri = PERIODICAL_LIST_URI,
			currentUser = notAdmin
		)

		assertPermissionsAreNotGranted
	}

	test("checkPermissions() when accessing 'Display Periodicals' Should return true for admin") {
		setupMocks(
			httpMethod = "get",
			requestUri = PERIODICAL_LIST_URI,
			currentUser = admin
		)

		assertPermissionsAreGranted
	}

	test("checkPermissions() when accessing 'Display Periodicals' Should return true for Not admin") {
		setupMocks(
			httpMethod = "get",
			requestUri = PERIODICAL_LIST_URI,
			currentUser = notAdmin
		)

		assertPermissionsAreGranted
	}

	test("checkPermissions() when accessing 'Display Users' Should return true for admin") {
		setupMocks(
			httpMethod = "get",
			requestUri = USERS_LIST_URI,
			currentUser = admin
		)

		assertPermissionsAreGranted
	}

	test("checkPermissions() when accessing 'Display Users' Should return false for Not admin") {
		setupMocks(
			httpMethod = "get",
			requestUri = USERS_LIST_URI,
			currentUser = notAdmin
		)

		assertPermissionsAreNotGranted
	}

	test("checkPermissions() when accessing 'Admin Panel' Should return true for admin") {
		setupMocks(
			httpMethod = "get",
			requestUri = ADMIN_PANEL_URI,
			currentUser = admin
		)

		assertPermissionsAreGranted
	}

	test("checkPermissions() when accessing 'Admin Panel' Should return false for Not admin") {
		setupMocks(
			httpMethod = "get",
			requestUri = ADMIN_PANEL_URI,
			currentUser = notAdmin
		)

		assertPermissionsAreNotGranted
	}

	test("checkPermissions() when accessing 'Update Periodical' Should return true for admin") {
		setupMocks(
			httpMethod = "get",
			requestUri = UPDATE_PERIODICAL_10,
			currentUser = admin
		)

		assertPermissionsAreGranted
	}

	test("checkPermissions() when accessing 'Update Periodical' Should return false for Not admin") {
		setupMocks(
			httpMethod = "get",
			requestUri = UPDATE_PERIODICAL_10,
			currentUser = notAdmin
		)

		assertPermissionsAreNotGranted
	}

	private def setupMocks(httpMethod: String, requestUri: String, currentUser: User) = {
		when(requestMock.getMethod) thenReturn httpMethod
		when(requestMock.getRequestURI) thenReturn requestUri
		when(sessionMock.getAttribute(CURRENT_USER_ATTR_NAME)) thenReturn currentUser
	}

	private def assertPermissionsAreGranted = {
		assert(Authorization.checkPermissions(requestMock))
	}

	private def assertPermissionsAreNotGranted = {
		assert(!Authorization.checkPermissions(requestMock))
	}
}
