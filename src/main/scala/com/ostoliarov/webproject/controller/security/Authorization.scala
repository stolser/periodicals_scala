package com.ostoliarov.webproject.controller.security

import com.ostoliarov.webproject.controller.ApplicationResources.CURRENT_USER_ATTR_NAME
import com.ostoliarov.webproject.controller.request.processor.RequestProviderImpl._
import com.ostoliarov.webproject.controller.utils.HttpUtils._
import com.ostoliarov.webproject.model.entity.user.UserRole.UserRole
import com.ostoliarov.webproject.model.entity.user.{User, UserRole}
import javax.servlet.http.{HttpServletRequest, HttpSession}

/**
	* Created by Oleg Stoliarov on 10/12/18.
	* Encapsulates information about resource access permissions of each type of roles.
	*/
object Authorization {
	private type RequestUriPattern = String
	private val USERS_URI_WITH_ID = "/backend/users/\\d+"
	private val admin = Set[UserRole](
		UserRole.ADMIN
	)
	private val permissionMapping = Map[RequestUriPattern, Set[UserRole]](
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

		permissionMappingForRequest(request) match {
			case Some(accessRestriction) => isPermissionGranted(accessRestriction, request)
			case None => true
		}
	}

	private def isUserIdInUriValid(request: HttpServletRequest) = {
		val requestUri = request.getRequestURI
		val userIdPattern = USERS_URI_WITH_ID.r
		if (userIdPattern.findFirstIn(requestUri).isDefined) {
			val userIdFromUri = firstIdFromUri(requestUri)
			userIdFromUri == userIdFromSession(request)
		} else true
	}

	private def permissionMappingForRequest(request: HttpServletRequest): Option[(RequestUriPattern, Set[UserRole])] =
		permissionMapping
			.filterKeys(filterRequestByHttpMethod(request, _))
			.filterKeys(filterRequestByUri(request, _))
			.headOption

	private def isPermissionGranted(permissionMapping: (RequestUriPattern, Set[UserRole]),
																	request: HttpServletRequest) =
		hasUserLegitRole(
			userRoles = userRolesFromSession(request.getSession),
			legitRoles = permissionMapping._2
		)

	private def userRolesFromSession(session: HttpSession) =
		session.getAttribute(CURRENT_USER_ATTR_NAME).asInstanceOf[User].roles

	private def hasUserLegitRole(userRoles: Set[UserRole],
															 legitRoles: Set[UserRole]) =
		(userRoles intersect legitRoles).nonEmpty
}
