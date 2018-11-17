package com.stolser.javatraining.webproject.controller.form.validator.periodical

import com.stolser.javatraining.webproject.FlatSpecScalaMockBase
import com.stolser.javatraining.webproject.controller.ApplicationResources.{MSG_PERIODICAL_PUBLISHER_ERROR, MSG_SUCCESS, STATUS_CODE_SUCCESS, STATUS_CODE_VALIDATION_FAILED}
import com.stolser.javatraining.webproject.controller.form.validator.ValidationResult
import javax.servlet.http.HttpServletRequest

/**
	* Created by Oleg Stoliarov on 11/17/18.
	*/
class PeriodicalPublisherValidatorTest extends FlatSpecScalaMockBase {

	private val successResult = ValidationResult(
		statusCode = STATUS_CODE_SUCCESS,
		messageKey = MSG_SUCCESS
	)
	private val failedResult = ValidationResult(
		statusCode = STATUS_CODE_VALIDATION_FAILED,
		messageKey = MSG_PERIODICAL_PUBLISHER_ERROR
	)
	private val requestStub = stub[HttpServletRequest]

	behavior of "PeriodicalPublisherValidator"

	it should "return the Successful result for a valid publisher name" in {
		val validPublisher = "Valid Publisher Name"

		assertThatForPublisherReturns(validPublisher, successResult)
	}

	it should "return a Failed result for a too short name" in {
		val tooShortName = "a"

		assertThatForPublisherReturns(tooShortName, failedResult)
	}

	it should "return a Failed result for a too long name" in {
		val tooLongName = "A tooooooooooooooooooooooooooooooooooooooo long name"

		assertThatForPublisherReturns(tooLongName, failedResult)
	}

	it should "return a Failed result for a name containing forbidden characters" in {
		val forbiddenChars = Set("[", "]", "{", "}", "%", "^", "*", "!", "@", "#", "$")

		forbiddenChars.foreach(badChar =>
			assertThatForPublisherReturns(s"A name containing $badChar", failedResult)
		)
	}

	private def assertThatForPublisherReturns(name: String, validationResult: ValidationResult) = {
		assert(PeriodicalPublisherValidator.validate(paramValue = name, request = requestStub) == validationResult)
	}
}
