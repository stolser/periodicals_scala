package com.ostoliarov.webproject.view.jsp.tag

import java.util.Objects.nonNull

import com.ostoliarov.webproject.controller.ApplicationResources
import com.ostoliarov.webproject.model.entity.user.{User, UserRole}
import javax.servlet.jsp.JspException
import javax.servlet.jsp.tagext.{Tag, TagSupport}

import scala.beans.BeanProperty

/**
	* Created by Oleg Stoliarov on 10/15/18.
	* Allows specifying two sets of roles that a user must have and must not have in order to
	* see the content of this tag.
	*/
class AuthorizationTag extends TagSupport {
	@BeanProperty
	var mustHaveRoles: String = _

	@BeanProperty
	var mustNotHaveRoles: String = _
	private var user: User = _

	@throws[JspException]
	override def doStartTag(): Int = {
		user = getUserFromSession
		if (nonNull(user)
			&& hasUserLegitRoles
			&& hasUserNoProhibitedRoles)
			Tag.EVAL_BODY_INCLUDE
		else
			Tag.SKIP_BODY
	}

	private def getUserFromSession = pageContext.getSession
		.getAttribute(ApplicationResources.CURRENT_USER_ATTR_NAME).asInstanceOf[User]

	private def hasUserLegitRoles: Boolean =
		mustHaveRoles match {
			case "*" => true
			case _ => (user.roles intersect parseUserRoles(mustHaveRoles)).nonEmpty
		}

	private def parseUserRoles(userRoles: String): Set[UserRole.Value] =
		userRoles match {
			case roles: String => roles.split(" ")
				.map((roleStr: String) => UserRole.withName(roleStr.toUpperCase()))
				.toSet
			case _ => Set()
		}

	private def hasUserNoProhibitedRoles: Boolean =
		mustNotHaveRoles match {
			case "*" => false
			case _ => (user.roles intersect parseUserRoles(mustNotHaveRoles)).isEmpty
		}
}
