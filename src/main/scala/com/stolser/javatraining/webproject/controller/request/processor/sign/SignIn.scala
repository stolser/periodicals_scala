package com.stolser.javatraining.webproject.controller.request.processor.sign

import java.util.Objects.nonNull

import com.stolser.javatraining.webproject.controller.ApplicationResources._
import com.stolser.javatraining.webproject.controller.message.{FrontMessageFactory, FrontendMessage}
import com.stolser.javatraining.webproject.controller.request.processor.RequestProcessor
import com.stolser.javatraining.webproject.controller.utils.HttpUtils
import com.stolser.javatraining.webproject.model.entity.user.{Credential, User, UserRole, UserStatus}
import com.stolser.javatraining.webproject.service.UserService
import com.stolser.javatraining.webproject.service.impl.UserServiceImpl
import javax.servlet.http.{HttpServletRequest, HttpServletResponse, HttpSession}

import scala.collection.mutable
import scala.collection.JavaConverters._

/**
  * Created by Oleg Stoliarov on 10/11/18.
  * Performs validation of the username, checks the password for correctness, checks that
  * this user is active (not blocked) and if everything is OK, adds this user into the session.
  */
object SignIn extends RequestProcessor {
	private val userService = UserServiceImpl
	private val messageFactory = FrontMessageFactory

	override def process(request: HttpServletRequest, response: HttpServletResponse): String = {
		val messages = mutable.Map[String, FrontendMessage]()
		val redirectUri =
			if (isCredentialCorrect(request))
				signInIfUserIsActive(request, messages)
			else {
				addSignInErrorMessages(messages)
				LOGIN_PAGE
			}

		setSessionAttributes(request, messages)

		REDIRECT + redirectUri
	}

	private def isCredentialCorrect(request: HttpServletRequest) =
		isPasswordCorrect(
			password = request.getParameter(USER_PASSWORD_PARAM_NAME),
			credential = getCredentialFromDb(request)
		)

	private def getCredentialFromDb(request: HttpServletRequest) =
		userService.findOneCredentialByUserName(
			userName = request.getParameter(SIGN_IN_USERNAME_PARAM_NAME)
		)

	private def isPasswordCorrect(password: String, credential: Credential) =
		nonNull(credential) &&
			(HttpUtils.getPasswordHash(password) == credential.getPasswordHash)

	private def signInIfUserIsActive(request: HttpServletRequest,
									 messages: mutable.Map[String, FrontendMessage]) = {
		val currentUser = userService.findOneUserByUserName(
			userName = request.getParameter(SIGN_IN_USERNAME_PARAM_NAME)
		)

		if (isUserActive(currentUser))
			signInUserAndGetRedirectUri(request, currentUser)
		else {
			messages.put(SIGN_IN_USERNAME_PARAM_NAME, messageFactory.getError(MSG_ERROR_USER_IS_BLOCKED))
			LOGIN_PAGE
		}
	}

	private def isUserActive(currentUser: User) =
		currentUser.getStatus == UserStatus.ACTIVE

	private def signInUserAndGetRedirectUri(request: HttpServletRequest, currentUser: User) = {
		val session = request.getSession
		val redirectUri = getRedirectUri(request, currentUser)
		session.setAttribute(CURRENT_USER_ATTR_NAME, currentUser)
		session.removeAttribute(ORIGINAL_URI_ATTR_NAME)

		redirectUri
	}

	private def addSignInErrorMessages(messages: mutable.Map[String, FrontendMessage]): Unit = {
		messages.put(SIGN_IN_USERNAME_PARAM_NAME, messageFactory.getError(MSG_CREDENTIALS_ARE_NOT_CORRECT))
		messages.put(USER_PASSWORD_PARAM_NAME, messageFactory.getError(MSG_CREDENTIALS_ARE_NOT_CORRECT))
	}

	private def getRedirectUri(request: HttpServletRequest, currentUser: User) = {
		val originalUri = request.getSession.getAttribute(ORIGINAL_URI_ATTR_NAME).asInstanceOf[String]
		val defaultUri =
			if (currentUser.hasRole(UserRole.ADMIN))
				ADMIN_PANEL_URI
			else
				CURRENT_USER_ACCOUNT_URI

		if (nonNull(originalUri) && SIGN_OUT_URI != originalUri)
			originalUri
		else
			defaultUri
	}

	private def setSessionAttributes(request: HttpServletRequest,
									 messages: mutable.Map[String, FrontendMessage]): Unit = {
		val session = request.getSession
		val username = request.getParameter(SIGN_IN_USERNAME_PARAM_NAME)
		session.setAttribute(USERNAME_ATTR_NAME, username)
		session.setAttribute(MESSAGES_ATTR_NAME, messages.asJava)
	}
}
