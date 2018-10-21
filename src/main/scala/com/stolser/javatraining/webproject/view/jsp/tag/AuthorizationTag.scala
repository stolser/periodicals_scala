package com.stolser.javatraining.webproject.view.jsp.tag

import java.util
import java.util.Objects.nonNull
import java.util.stream.Collectors

import com.stolser.javatraining.webproject.controller.ApplicationResources
import com.stolser.javatraining.webproject.model.entity.user.{User, UserRole}
import javax.servlet.jsp.JspException
import javax.servlet.jsp.tagext.{Tag, TagSupport}

import scala.beans.BeanProperty
import scala.collection.JavaConverters._
import scala.collection.mutable

/**
  * Created by Oleg Stoliarov on 10/15/18.
  * Allows specifying two sets of roles that a user must have and must not have in order to
  * see the content of this tag.
  */
class AuthorizationTag extends TagSupport {
	private var mustHaveRoles: String = _
	private var mustNotHaveRoles: String = _
	private var user: User = _

	@throws[JspException]
	override def doStartTag(): Int = {
		user = getUserFromSession
		if (nonNull(user) &&
			hasUserLegitRoles &&
			hasUserNoProhibitedRoles)
			Tag.EVAL_BODY_INCLUDE
		else
			Tag.SKIP_BODY
	}

	private def getUserFromSession = pageContext.getSession
		.getAttribute(ApplicationResources.CURRENT_USER_ATTR_NAME).asInstanceOf[User]

	private def hasUserLegitRoles: Boolean =
		if ("*" == mustHaveRoles)
			true
		else {
			val legitRoles = parseUserRoles(mustHaveRoles).asJava
			val userRoles = new util.HashSet[UserRole.Value](user.getRoles.asJava)
			userRoles.retainAll(legitRoles)

			!userRoles.isEmpty
		}

	private def parseUserRoles(userRoles: String) = {
		if (nonNull(userRoles)) {
			userRoles.split(" ")
				.map((roleStr: String) => UserRole.withName(roleStr.toUpperCase()))
				.toSet
		} else
			Set()
	}

	private def hasUserNoProhibitedRoles =
		if ("*" == mustNotHaveRoles)
			false
		else {
			val prohibitedRoles = parseUserRoles(mustNotHaveRoles).asJava
			val userRoles = new util.HashSet[UserRole.Value](user.getRoles.asJava)
			userRoles.retainAll(prohibitedRoles)

			userRoles.isEmpty
		}

	def getMustHaveRoles: String = mustHaveRoles

	def setMustHaveRoles(mustHaveRoles: String): Unit =
		this.mustHaveRoles = mustHaveRoles

	def getMustNotHaveRoles: String = mustNotHaveRoles

	def setMustNotHaveRoles(mustNotHaveRoles: String): Unit =
		this.mustNotHaveRoles = mustNotHaveRoles
}
