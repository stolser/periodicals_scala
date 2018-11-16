package com.stolser.javatraining.webproject.controller.security

import com.stolser.javatraining.webproject.controller.ApplicationResources.CURRENT_USER_ATTR_NAME
import com.stolser.javatraining.webproject.controller.request.processor.RequestProviderImpl._
import com.stolser.javatraining.webproject.controller.utils.HttpUtils
import com.stolser.javatraining.webproject.controller.utils.HttpUtils.{filterRequestByHttpMethod, filterRequestByUri}
import com.stolser.javatraining.webproject.model.entity.user.{User, UserRole}
import javax.servlet.http.{HttpServletRequest, HttpSession}

/**
	* Created by Oleg Stoliarov on 10/12/18.
	* Encapsulates information about resource access permissions of each type of roles.
	*/
object Authorization {
	private val USERS_URI_WITH_ID = "/backend/users/\\d+"
	private val admin = Set[UserRole.Value](
		UserRole.ADMIN
	)
	private val permissionMapping = Map[String, Set[UserRole.Value]](
		GET_ALL_USERS_REQUEST_PATTERN -> admin,
		GET_CREATE_PERIODICAL_REQUEST_PATTERN -> admin,
		GET_UPDATE_PERIODICAL_REQUEST_PATTERN -> admin,
		POST_PERSIST_PERIODICAL_REQUEST_PATTERN -> admin,
		POST_DELETE_PERIODICALS_REQUEST_PATTERN -> admin,
		GET_ADMIN_PANEL_REQUEST_PATTERN -> admin,
	)

	/**
		* Checks whether a current user has enough permissions to access a requested uri
		* using a current http method.
		*
		* @param request a current http request
		* @return { @code true} - if a current user has enough permissions to perform such a kind of requests,
		*         and { @code false} otherwise
		*/
	private[security] def checkPermissions(request: HttpServletRequest): Boolean = {
		if (!isUserIdInUriValid(request))
			return false

		getPermissionMapping(request) match {
			case Some(accessRestriction) => isPermissionGranted(accessRestriction, request)
			case None => true
		}
	}

	private def isUserIdInUriValid(request: HttpServletRequest) = {
		val requestUri = request.getRequestURI
		val userIdPattern = USERS_URI_WITH_ID.r
		if (userIdPattern.findFirstIn(requestUri).isDefined) {
			val userIdFromUri = HttpUtils.firstIdFromUri(requestUri)
			val userIdFromSession = HttpUtils.userIdFromSession(request)
			userIdFromUri == userIdFromSession
		} else true
	}

	private def getPermissionMapping(request: HttpServletRequest): Option[(String, Set[UserRole.Value])] =
		permissionMapping
			.filterKeys(filterRequestByHttpMethod(request, _))
			.filterKeys(filterRequestByUri(request, _))
			.headOption

	private def isPermissionGranted(permissionMapping: (String, Set[UserRole.Value]),
																	request: HttpServletRequest) =
		hasUserLegitRole(
			userRoles = getUserRolesFromSession(request.getSession),
			legitRoles = permissionMapping._2
		)

	private def getUserRolesFromSession(session: HttpSession) =
		session.getAttribute(CURRENT_USER_ATTR_NAME).asInstanceOf[User].roles

	private def hasUserLegitRole(userRoles: Set[UserRole.Value],
															 legitRoles: Set[UserRole.Value]) =
		(userRoles intersect legitRoles).nonEmpty
}