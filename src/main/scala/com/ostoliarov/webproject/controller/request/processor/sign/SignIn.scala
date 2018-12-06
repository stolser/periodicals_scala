package com.ostoliarov.webproject.controller.request.processor.sign

import com.ostoliarov.eventsourcing.EventLoggingHelper
import com.ostoliarov.eventsourcing.model.SignInEvent
import com.ostoliarov.webproject.controller.ApplicationResources._
import com.ostoliarov.webproject.controller.message.{FrontMessageFactory, FrontendMessage}
import com.ostoliarov.webproject.controller.request.processor.DispatchType.REDIRECT
import com.ostoliarov.webproject.controller.request.processor.{AbstractViewName, RequestProcessor, ResourceRequest}
import com.ostoliarov.webproject.controller.utils.HttpUtils._
import com.ostoliarov.webproject.model.entity.user.{Credential, User, UserRole, UserStatus}
import com.ostoliarov.webproject.service.impl.mysql.UserServiceMysqlImpl
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
	* Created by Oleg Stoliarov on 10/11/18.
	* Performs validation of the username, checks the password for correctness, checks that
	* this user is active (not blocked) and if everything is OK, adds this user into the session.
	*/
object SignIn extends RequestProcessor {
	private val LOGGER = LoggerFactory.getLogger(SignIn.getClass)
	private type ParamName = String
	private val userService = UserServiceMysqlImpl
	private val messageFactory = FrontMessageFactory

	override def process(request: HttpServletRequest,
											 response: HttpServletResponse): ResourceRequest = {
		val messages = mutable.Map[ParamName, FrontendMessage]()
		val redirectUri =
			if (isCredentialCorrect(request))
				signInIfUserIsActive(request, messages)
			else {
				addSignInErrorMessages(messages)
				LOGIN_PAGE
			}

		setSessionAttributes(request, messages)

		ResourceRequest(REDIRECT, AbstractViewName(redirectUri))
	}

	private def isCredentialCorrect(request: HttpServletRequest) =
		isPasswordCorrect(
			password = request.getParameter(USER_PASSWORD_PARAM_NAME),
			credentialOpt = getCredentialFromDb(request)
		)

	private def getCredentialFromDb(request: HttpServletRequest) =
		userService.findOneCredentialByUserName(userName = request.getParameter(SIGN_IN_USERNAME_PARAM_NAME))

	private def isPasswordCorrect(password: String,
																credentialOpt: Option[Credential]) =
		credentialOpt match {
			case Some(credential) => passwordHash(password) == credential.passwordHash
			case None => false
		}

	private def signInIfUserIsActive(request: HttpServletRequest,
																	 messages: mutable.Map[ParamName, FrontendMessage]) = {
		val currentUser = userService.findOneByName(
			userName = request.getParameter(SIGN_IN_USERNAME_PARAM_NAME)
		)

		currentUser match {
			case None => LOGIN_PAGE
			case Some(user) =>
				if (isUserActive(user))
					signInUserAndGetRedirectUri(request, user)
				else {
					messages.put(SIGN_IN_USERNAME_PARAM_NAME, messageFactory.error(MSG_ERROR_USER_IS_BLOCKED))
					LOGIN_PAGE
				}
		}
	}

	private def isUserActive(currentUser: User) =
		currentUser.status == UserStatus.ACTIVE

	private def signInUserAndGetRedirectUri(request: HttpServletRequest, currentUser: User) = {
		val session = request.getSession
		session.setAttribute(CURRENT_USER_ATTR_NAME, currentUser)
		session.removeAttribute(ORIGINAL_URI_ATTR_NAME)

		EventLoggingHelper.logEvent(SignInEvent(currentUser.id, request.getHeader("User-Agent")))

		redirectUri(request, currentUser)
	}

	private def addSignInErrorMessages(messages: mutable.Map[ParamName, FrontendMessage]): Unit = {
		messages.put(SIGN_IN_USERNAME_PARAM_NAME, messageFactory.error(MSG_CREDENTIALS_ARE_NOT_CORRECT))
		messages.put(USER_PASSWORD_PARAM_NAME, messageFactory.error(MSG_CREDENTIALS_ARE_NOT_CORRECT))
	}

	private def redirectUri(request: HttpServletRequest,
													currentUser: User) = {
		val originalUriOpt = Option(request.getSession.getAttribute(ORIGINAL_URI_ATTR_NAME).asInstanceOf[String])

		originalUriOpt match {
			case Some(originalUri) if SIGN_OUT_URI != originalUri => originalUri
			case None => if (currentUser.hasRole(UserRole.ADMIN))
				ADMIN_PANEL_URI
			else
				CURRENT_USER_ACCOUNT_URI
		}
	}

	private def setSessionAttributes(request: HttpServletRequest,
																	 messages: mutable.Map[ParamName, FrontendMessage]): Unit = {
		val session = request.getSession
		val username = request.getParameter(SIGN_IN_USERNAME_PARAM_NAME)
		session.setAttribute(USERNAME_ATTR_NAME, username)
		session.setAttribute(MESSAGES_ATTR_NAME, messages.asJava)
	}
}
