package com.stolser.javatraining.webproject.controller.security

import java.util
import java.util.regex.{Matcher, Pattern}
import java.util.{Collections, HashMap, HashSet, Map, Optional, Set}

import com.stolser.javatraining.webproject.controller.ApplicationResources.CURRENT_USER_ATTR_NAME
import com.stolser.javatraining.webproject.model.entity.user.User
import com.stolser.javatraining.webproject.controller.request.processor.RequestProviderImpl._
import com.stolser.javatraining.webproject.controller.utils.HttpUtils
import com.stolser.javatraining.webproject.controller.utils.HttpUtils.{filterRequestByHttpMethod, filterRequestByUri}
import javax.servlet.http.HttpServletRequest

/**
  * Created by Oleg Stoliarov on 10/12/18.
  * Encapsulates information about resource access permissions of each type of roles.
  */
object Authorization {
	private val permissionMapping = new util.HashMap[String, util.Set[User.Role]]
	private val USERS_URI_WITH_ID = "/backend/users/\\d+"

	val admin = new util.HashSet[User.Role](Collections.singletonList(User.Role.ADMIN))

	permissionMapping.put(GET_ALL_USERS_REQUEST_PATTERN, admin)
	permissionMapping.put(GET_CREATE_PERIODICAL_REQUEST_PATTERN, admin)
	permissionMapping.put(GET_UPDATE_PERIODICAL_REQUEST_PATTERN, admin)
	permissionMapping.put(POST_PERSIST_PERIODICAL_REQUEST_PATTERN, admin)
	permissionMapping.put(POST_DELETE_PERIODICALS_REQUEST_PATTERN, admin)
	permissionMapping.put(GET_ADMIN_PANEL_REQUEST_PATTERN, admin)

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

		val accessRestriction = getPermissionMapping(request)
		if (accessRestriction.isPresent) return isPermissionGranted(accessRestriction.get, request)
		true
	}

	private def isUserIdInUriValid(request: HttpServletRequest) = {
		val requestUri = request.getRequestURI
		val matcher = Pattern.compile(USERS_URI_WITH_ID).matcher(requestUri)

		if (matcher.find) {
			val userIdFromUri = HttpUtils.getFirstIdFromUri(requestUri)
			val userIdFromSession = HttpUtils.getUserIdFromSession(request)
			userIdFromUri == userIdFromSession
		} else true
	}

	private def getPermissionMapping(request: HttpServletRequest) =
		permissionMapping.entrySet.stream
			.filter((entry: util.Map.Entry[String, util.Set[User.Role]]) => filterRequestByHttpMethod(request, entry.getKey))
			.filter((entry: util.Map.Entry[String, util.Set[User.Role]]) => filterRequestByUri(request, entry.getKey))
			.findFirst

	private def isPermissionGranted(permissionMapping: util.Map.Entry[String, util.Set[User.Role]], request: HttpServletRequest) = {
		val userRoles = getUserFromSession(request).getRoles
		val legitRoles = permissionMapping.getValue
		hasUserLegitRole(userRoles, legitRoles)
	}

	private def getUserFromSession(request: HttpServletRequest) = request.getSession.getAttribute(CURRENT_USER_ATTR_NAME).asInstanceOf[User]

	private def hasUserLegitRole(userRoles: util.Set[User.Role], legitRoles: util.Set[User.Role]) = {
		val userLegitRoles = new util.HashSet[User.Role](legitRoles)
		userLegitRoles.retainAll(userRoles)
		!userLegitRoles.isEmpty
	}
}
