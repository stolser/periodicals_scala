package com.ostoliarov.webproject.controller.request.processor.user

import com.ostoliarov.webproject.controller.ApplicationResources._
import com.ostoliarov.webproject.controller.form.validator.ValidatorFactory
import com.ostoliarov.webproject.controller.message.{FrontMessageFactory, FrontendMessage}
import com.ostoliarov.webproject.controller.request.processor.DispatchType.REDIRECT
import com.ostoliarov.webproject.controller.request.processor.{AbstractViewName, RequestProcessor, ResourceRequest}
import com.ostoliarov.webproject.controller.utils.HttpUtils._
import com.ostoliarov.webproject.model.entity.user.UserRole.UserRole
import com.ostoliarov.webproject.model.entity.user.{Credential, User, UserRole, UserStatus}
import com.ostoliarov.webproject.service.impl.mysql.UserServiceMysqlImpl
import javax.servlet.http.{HttpServletRequest, HttpServletResponse, HttpSession}

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
	* Created by Oleg Stoliarov on 10/11/18.
	*/
object CreateUser extends RequestProcessor {
	private type ParamName = String
	private val userService = UserServiceMysqlImpl
	private val messageFactory = FrontMessageFactory

	override def process(request: HttpServletRequest,
											 response: HttpServletResponse): ResourceRequest = {
		val formMessages = mutable.Map[ParamName, FrontendMessage]()
		val generalMessages = mutable.ListBuffer[FrontendMessage]()
		val session: HttpSession = request.getSession
		var redirectUri: String = SIGN_UP_URI

		val username: String = request.getParameter(SIGN_UP_USERNAME_PARAM_NAME)
		val userEmail: String = request.getParameter(USER_EMAIL_PARAM_NAME)
		val password: String = request.getParameter(USER_PASSWORD_PARAM_NAME)
		val repeatPassword: String = request.getParameter(USER_REPEAT_PASSWORD_PARAM_NAME)
		val userRole: UserRole = UserRole.withName(request.getParameter(USER_ROLE_PARAM_NAME).toUpperCase)

		if (!arePasswordsValidAndEqual(password, repeatPassword))
			formMessages.put(USER_PASSWORD_PARAM_NAME, messageFactory.error(MSG_VALIDATION_PASSWORDS_ARE_NOT_EQUAL))
		else if (usernameExistsInDb(username))
			formMessages.put(SIGN_UP_USERNAME_PARAM_NAME, messageFactory.error(USERNAME_IS_NOT_UNIQUE_TRY_ANOTHER_ONE))
		else {
			val isNewUserCreated: Boolean = createUser(username, userEmail, password, userRole)
			if (isNewUserCreated) redirectUri = USERS_LIST_URI
			else generalMessages += messageFactory.error(MSG_NEW_USER_WAS_NOT_CREATED_ERROR)
		}

		if (SIGN_UP_URI == redirectUri) {
			addGeneralMessagesToSession(request, generalMessages)

			session.setAttribute(USERNAME_ATTR_NAME, username)
			session.setAttribute(USER_ROLE_ATTR_NAME, userRole)
			session.setAttribute(USER_EMAIL_ATTR_NAME, userEmail)
			session.setAttribute(MESSAGES_ATTR_NAME, formMessages.asJava)
		}

		ResourceRequest(REDIRECT, AbstractViewName(redirectUri))
	}

	private def createUser(username: String,
												 userEmail: String,
												 password: String,
												 userRole: UserRole) =
		userService.createNewUser(
			User(
				status = UserStatus.ACTIVE,
				email = userEmail),
			Credential(
				userName = username,
				passwordHash = passwordHash(password)),
			userRole)

	private def arePasswordsValidAndEqual(password: String, repeatPassword: String) = {
		val validationResult = ValidatorFactory.userPasswordValidator.validate(password, null).statusCode
		((validationResult == STATUS_CODE_SUCCESS)
			&& (password == repeatPassword))
	}

	private def usernameExistsInDb(username: String) = userService.findOneCredentialByUserName(username).nonEmpty
}
