package com.stolser.javatraining.webproject.controller.security

import com.stolser.javatraining.webproject.FunSuiteBase
import com.stolser.javatraining.webproject.TestResources._
import com.stolser.javatraining.webproject.controller.ApplicationResources._
import com.stolser.javatraining.webproject.model.entity.user.{User, UserRole}
import javax.servlet.http.{HttpServletRequest, HttpSession}

/**
	* Created by Oleg Stoliarov on 11/5/18.
	*/
class AuthorizationTest extends FunSuiteBase {
	private var session: HttpSession = _
	private var request: HttpServletRequest = _
	private val admin = User(roles = Set(UserRole.ADMIN))
	private val notAdmin = User(roles = Set(UserRole.SUBSCRIBER))

	before {
		session = mock[HttpSession]
		request = mock[HttpServletRequest]
		when(request.getSession) thenReturn session
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
		when(request.getMethod) thenReturn httpMethod
		when(request.getRequestURI) thenReturn requestUri
		when(session.getAttribute(CURRENT_USER_ATTR_NAME)) thenReturn currentUser
	}

	private def assertPermissionsAreGranted = {
		assert(Authorization.checkPermissions(request))
	}

	private def assertPermissionsAreNotGranted = {
		assert(!Authorization.checkPermissions(request))
	}
}
