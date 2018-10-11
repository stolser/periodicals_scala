package com.stolser.javatraining.webproject.controller.request.processor.user

import java.util
import java.util.Objects.nonNull
import java.util.{ArrayList, HashMap, List, Map}

import com.stolser.javatraining.webproject.controller.ApplicationResources._
import com.stolser.javatraining.webproject.controller.form.validator.ValidatorFactory
import com.stolser.javatraining.webproject.controller.message.{FrontMessageFactory, FrontendMessage}
import com.stolser.javatraining.webproject.controller.request.processor.RequestProcessor
import com.stolser.javatraining.webproject.controller.utils.HttpUtils
import com.stolser.javatraining.webproject.model.entity.user.{Credential, User}
import com.stolser.javatraining.webproject.service.UserService
import com.stolser.javatraining.webproject.service.impl.UserServiceImpl
import javax.servlet.http.{HttpServletRequest, HttpServletResponse, HttpSession}

/**
  * Created by Oleg Stoliarov on 10/11/18.
  */
object CreateUser extends RequestProcessor {
	private val userService = UserServiceImpl.getInstance
	private val messageFactory = FrontMessageFactory

	override def process(request: HttpServletRequest, response: HttpServletResponse): String = {
		val formMessages: util.Map[String, FrontendMessage] = new util.HashMap[String, FrontendMessage]
		val generalMessages: util.List[FrontendMessage] = new util.ArrayList[FrontendMessage]
		val session: HttpSession = request.getSession
		var redirectUri: String = SIGN_UP_URI

		val username: String = request.getParameter(SIGN_UP_USERNAME_PARAM_NAME)
		val userEmail: String = request.getParameter(USER_EMAIL_PARAM_NAME)
		val password: String = request.getParameter(USER_PASSWORD_PARAM_NAME)
		val repeatPassword: String = request.getParameter(USER_REPEAT_PASSWORD_PARAM_NAME)
		val userRole: User.Role = User.Role.valueOf(request.getParameter(USER_ROLE_PARAM_NAME).toUpperCase)

		if (!arePasswordsValidAndEqual(password, repeatPassword))
			formMessages.put(USER_PASSWORD_PARAM_NAME, messageFactory.getError(MSG_VALIDATION_PASSWORDS_ARE_NOT_EQUAL))
		else if (usernameExistsInDb(username))
			formMessages.put(SIGN_UP_USERNAME_PARAM_NAME, messageFactory.getError(USERNAME_IS_NOT_UNIQUE_TRY_ANOTHER_ONE))
		else {
			val isNewUserCreated: Boolean = createUser(username, userEmail, password, userRole)
			if (isNewUserCreated) redirectUri = LOGIN_PAGE
			else generalMessages.add(messageFactory.getError(MSG_NEW_USER_WAS_NOT_CREATED_ERROR))
		}

		if (SIGN_UP_URI == redirectUri) {
			HttpUtils.addGeneralMessagesToSession(request, generalMessages)
			session.setAttribute(USERNAME_ATTR_NAME, username)
			session.setAttribute(USER_ROLE_ATTR_NAME, userRole)
			session.setAttribute(USER_EMAIL_ATTR_NAME, userEmail)
			session.setAttribute(MESSAGES_ATTR_NAME, formMessages)
		}

		REDIRECT + redirectUri
	}

	private def createUser(username: String, userEmail: String, password: String, userRole: User.Role) = {
		val credentialBuilder = new Credential.Builder
		credentialBuilder.setUserName(username).setPasswordHash(HttpUtils.getPasswordHash(password))
		val userBuilder = new User.Builder
		userBuilder.setStatus(User.Status.ACTIVE)
		userBuilder.setEmail(userEmail)
		userService.createNewUser(userBuilder.build, credentialBuilder.build, userRole)
	}

	private def arePasswordsValidAndEqual(password: String, repeatPassword: String) = {
		val validationResult = ValidatorFactory.getUserPasswordValidator.validate(password, null).statusCode
		(validationResult == STATUS_CODE_SUCCESS) &&
			(password == repeatPassword)
	}

	private def usernameExistsInDb(username: String) = nonNull(userService.findOneCredentialByUserName(username))
}