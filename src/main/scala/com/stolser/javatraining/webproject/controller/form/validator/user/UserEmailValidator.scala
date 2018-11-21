package com.stolser.javatraining.webproject.controller.form.validator.user

import com.stolser.javatraining.webproject.controller.ApplicationResources.{MSG_USER_EMAIL_DUPLICATION_ERROR, MSG_USER_EMAIL_REGEX_ERROR, STATUS_CODE_VALIDATION_FAILED, USER_EMAIL_PATTERN_REGEX}
import com.stolser.javatraining.webproject.controller.form.validator.{AbstractValidator, ValidationResult}
import com.stolser.javatraining.webproject.service.UserService
import com.stolser.javatraining.webproject.service.impl.mysql.UserServiceMysqlImpl
import javax.servlet.http.HttpServletRequest

/**
	* Created by Oleg Stoliarov on 10/7/18.
	*/
object UserEmailValidator extends AbstractValidator {
	private var userServiceImpl = UserServiceMysqlImpl: UserService
	private val regexFailedResult = ValidationResult(
		statusCode = STATUS_CODE_VALIDATION_FAILED,
		messageKey = MSG_USER_EMAIL_REGEX_ERROR
	)
	private val duplicationFailedResult = ValidationResult(
		statusCode = STATUS_CODE_VALIDATION_FAILED,
		messageKey = MSG_USER_EMAIL_DUPLICATION_ERROR
	)

	override protected def checkParameter(userEmail: String,
																				request: HttpServletRequest): Option[ValidationResult] = {
		if (emailIsNotValid(userEmail)) return Some(regexFailedResult)
		if (emailExistsInDb(userEmail)) return Some(duplicationFailedResult)

		Option.empty[ValidationResult]
	}

	private def emailIsNotValid(userEmail: String) = !userEmail.matches(USER_EMAIL_PATTERN_REGEX)

	private def emailExistsInDb(userEmail: String) = userServiceImpl.emailExistsInDb(userEmail)

	private[user] def userService: UserService = userServiceImpl

	private[user] def userService_=(userService: UserService): Unit = {
		require(userService != null)

		this.userServiceImpl = userService
	}
}
