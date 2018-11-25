package com.ostoliarov.webproject.controller.form.validator.user

import com.ostoliarov.webproject.FlatSpecWithScalaMockBase
import com.ostoliarov.webproject.controller.ApplicationResources._
import com.ostoliarov.webproject.controller.form.validator.ValidationResult
import com.ostoliarov.webproject.service.UserService
import javax.servlet.http.HttpServletRequest

/**
	* Created by Oleg Stoliarov on 11/17/18.
	*/
class UserEmailValidatorTest extends FlatSpecWithScalaMockBase {
	private val successResult = ValidationResult(
		statusCode = STATUS_CODE_SUCCESS,
		messageKey = MSG_SUCCESS
	)
	private val regexFailedResult = ValidationResult(
		statusCode = STATUS_CODE_VALIDATION_FAILED,
		messageKey = MSG_USER_EMAIL_REGEX_ERROR
	)
	private val duplicationFailedResult = ValidationResult(
		statusCode = STATUS_CODE_VALIDATION_FAILED,
		messageKey = MSG_USER_EMAIL_DUPLICATION_ERROR
	)
	private val requestStub = stub[HttpServletRequest]
	private val userServiceStub = stub[UserService]
	private val userEmailValidator = UserEmailValidator
	userEmailValidator.userService = userServiceStub

	behavior of "UserEmailValidator"

	it should "return the Successful result for a valid and unique email" in {
		val validEmail = "oleh.stoliarov@example.com"

		(userServiceStub.emailExistsInDb _).when(validEmail).returns(false)

		assertThatForEmailReturns(validEmail, successResult)
	}

	it should "return a Failed result for an invalid email" in {
		val invalidEmails = Set("Just a string", "stoliarov.com", "stoliarov@com", "stoliarov@example.1", "@google.com")

		invalidEmails.foreach(email =>
			assertThatForEmailReturns(email, regexFailedResult)
		)
	}

	it should "return a Failed result for a valid but NOT unique email" in {
		val validEmail = "oleh.stoliarov@example.com"

		(userServiceStub.emailExistsInDb _).when(validEmail).returns(true)

		assertThatForEmailReturns(validEmail, duplicationFailedResult)
	}

	private def assertThatForEmailReturns(email: String, validationResult: ValidationResult) = {
		assert(userEmailValidator.validate(paramValue = email, request = requestStub) == validationResult)
	}
}
