package com.ostoliarov.webproject.controller.form.validator.user

import com.ostoliarov.webproject.controller.ApplicationResources.{MSG_USER_PASSWORD_ERROR, STATUS_CODE_VALIDATION_FAILED, USER_PASSWORD_PATTERN_REGEX}
import com.ostoliarov.webproject.controller.form.validator.{AbstractValidator, ValidationResult}
import javax.servlet.http.HttpServletRequest

/**
	* Created by Oleg Stoliarov on 10/8/18.
	*/
object UserPasswordValidator extends AbstractValidator {
	private val failedResult = ValidationResult(
		statusCode = STATUS_CODE_VALIDATION_FAILED,
		messageKey = MSG_USER_PASSWORD_ERROR
	)

	override protected def checkParameter(password: String,
																				request: HttpServletRequest): Option[ValidationResult] = {
		if (isPasswordNotValid(password)) return Some(failedResult)

		Option.empty[ValidationResult]
	}

	private def isPasswordNotValid(password: String) = !password.matches(USER_PASSWORD_PATTERN_REGEX)
}
