package com.ostoliarov.webproject.controller.form.validator.user

import com.ostoliarov.webproject.FlatSpecWithScalaMockBase
import com.ostoliarov.webproject.controller.ApplicationResources.{MSG_SUCCESS, MSG_USER_PASSWORD_ERROR, STATUS_CODE_SUCCESS, STATUS_CODE_VALIDATION_FAILED}
import com.ostoliarov.webproject.controller.form.validator.ValidationResult
import javax.servlet.http.HttpServletRequest

/**
	* Created by Oleg Stoliarov on 11/17/18.
	*/
class UserPasswordValidatorTest extends FlatSpecWithScalaMockBase {
	private val successResult = ValidationResult(
		statusCode = STATUS_CODE_SUCCESS,
		messageKey = MSG_SUCCESS
	)
	private val failedResult = ValidationResult(
		statusCode = STATUS_CODE_VALIDATION_FAILED,
		messageKey = MSG_USER_PASSWORD_ERROR
	)
	private val requestStub = stub[HttpServletRequest]

	behavior of "UserPasswordValidator"

	it should "return the Successful result for a valid password" in {
		val validPasswords = Set("123456", "abcdefghijkl", "abc!@#&{}", "a*()=/Z", "abc_<>_")

		validPasswords.foreach(password =>
			assertThatForPasswordReturns(password, successResult)
		)
	}

	it should "return a Failed result for a too short password" in {
		val tooShortPassword = "abc"

		assertThatForPasswordReturns(tooShortPassword, failedResult)
	}

	it should "return a Failed result for a too long password" in {
		val tooLongPassword = "A tooooooooooooooooooooooooooooooooooooooo long password"

		assertThatForPasswordReturns(tooLongPassword, failedResult)
	}

	private def assertThatForPasswordReturns(password: String, validationResult: ValidationResult) = {
		assert(UserPasswordValidator.validate(paramValue = password, request = requestStub) == validationResult)
	}
}
