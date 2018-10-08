package com.stolser.javatraining.webproject.controller.form.validator.user

import java.util.Optional
import java.util.regex.Pattern

import com.stolser.javatraining.webproject.controller.ApplicationResources.{
	MSG_USER_PASSWORD_ERROR,
	STATUS_CODE_VALIDATION_FAILED,
	USER_PASSWORD_PATTERN_REGEX
}
import com.stolser.javatraining.webproject.controller.form.validator.{AbstractValidator, ValidationResult}
import javax.servlet.http.HttpServletRequest

/**
  * Created by Oleg Stoliarov on 10/8/18.
  */
object UserPasswordValidator extends AbstractValidator {
	private val failedResult = new ValidationResult(STATUS_CODE_VALIDATION_FAILED, MSG_USER_PASSWORD_ERROR)

	override protected def checkParameter(password: String, request: HttpServletRequest): Optional[ValidationResult] =
		if (passwordMatchesRegex(password)) Optional.empty[ValidationResult]
		else Optional.of(failedResult)

	private def passwordMatchesRegex(password: String) = Pattern.matches(USER_PASSWORD_PATTERN_REGEX, password)
}
