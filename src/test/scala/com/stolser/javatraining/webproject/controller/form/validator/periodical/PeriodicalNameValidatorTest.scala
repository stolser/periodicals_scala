package com.stolser.javatraining.webproject.controller.form.validator.periodical

import com.stolser.javatraining.webproject.FlatSpecScalaMockBase
import com.stolser.javatraining.webproject.controller.ApplicationResources._
import com.stolser.javatraining.webproject.controller.form.validator.{ValidationProcessorException, ValidationResult}
import com.stolser.javatraining.webproject.model.entity.periodical.Periodical
import com.stolser.javatraining.webproject.service.PeriodicalService
import javax.servlet.http.HttpServletRequest

/**
	* Created by Oleg Stoliarov on 11/17/18.
	*/
class PeriodicalNameValidatorTest extends FlatSpecScalaMockBase {
	private val successResult = ValidationResult(
		statusCode = STATUS_CODE_SUCCESS,
		messageKey = MSG_SUCCESS
	)
	private val incorrectFailedResult = ValidationResult(
		statusCode = STATUS_CODE_VALIDATION_FAILED,
		messageKey = MSG_PERIODICAL_NAME_INCORRECT
	)
	private val duplicationFailedResult = ValidationResult(
		statusCode = STATUS_CODE_VALIDATION_FAILED,
		messageKey = MSG_PERIODICAL_NAME_DUPLICATION
	)
	private val requestStub = stub[HttpServletRequest]
	private val periodicalServiceStub = stub[PeriodicalService]
	private val periodicalNameValidator = PeriodicalNameValidator
	periodicalNameValidator.periodicalService = periodicalServiceStub

	behavior of "PeriodicalNameValidator"

	it should "return the Successful result for a unique and valid periodical name" in {
		val validAndUniqueName = "Valid and Unique Periodical Name #1"
		(periodicalServiceStub.findOneByName _).when(validAndUniqueName).returns(None)

		assertThatForPeriodicalNameReturns(validAndUniqueName, successResult)
	}

	it should "return a Successful result for a unique name and operation UPDATE" in {
		val periodicalIdFromRequest = "10"
		val periodicalIdFromDb = 10
		val validAndUniqueName = "Valid and unique name"
		val updateOperationType = "UPDATE"
		(requestStub.getParameter _).when(PERIODICAL_OPERATION_TYPE_PARAM_ATTR_NAME).returns(updateOperationType)
		(requestStub.getParameter _).when(ENTITY_ID_PARAM_NAME).returns(periodicalIdFromRequest)
		(periodicalServiceStub.findOneByName _).when(validAndUniqueName).returns(Some(Periodical(id = periodicalIdFromDb)))

		assertThatForPeriodicalNameReturns(validAndUniqueName, successResult)
	}

	it should "return a Failed result for a too short name" in {
		val tooShortName = "a"

		assertThatForPeriodicalNameReturns(tooShortName, incorrectFailedResult)
	}

	it should "return a Failed result for a too long name" in {
		val tooLongName = "A tooooooooooooooooooooooooooooooooooooooo long name"

		assertThatForPeriodicalNameReturns(tooLongName, incorrectFailedResult)
	}

	it should "return a Failed result for a name containing forbidden characters" in {
		val forbiddenChars = Set("[", "]", "{", "}", "%", "^", "*")

		forbiddenChars.foreach(badChar =>
			assertThatForPeriodicalNameReturns(s"A name containing $badChar", incorrectFailedResult)
		)
	}

	it should "return a Failed result for a NOT unique name and operation CREATE" in {
		val notUniqueName = "Valid but NOT unique name"
		val createOperationType = "Create"
		(periodicalServiceStub.findOneByName _).when(notUniqueName).returns(Some(Periodical()))
		(requestStub.getParameter _).when(PERIODICAL_OPERATION_TYPE_PARAM_ATTR_NAME).returns(createOperationType)

		assertThatForPeriodicalNameReturns(notUniqueName, duplicationFailedResult)
	}

	it should "return a Failed result for a NOT unique name and operation UPDATE" in {
		val periodicalIdFromRequest = "10"
		val periodicalIdFromDb = 22
		val notUniqueName = "Valid but NOT unique name"
		val updateOperationType = "update"
		(requestStub.getParameter _).when(PERIODICAL_OPERATION_TYPE_PARAM_ATTR_NAME).returns(updateOperationType)
		(requestStub.getParameter _).when(ENTITY_ID_PARAM_NAME).returns(periodicalIdFromRequest)
		(periodicalServiceStub.findOneByName _).when(notUniqueName).returns(Some(Periodical(id = periodicalIdFromDb)))

		assertThatForPeriodicalNameReturns(notUniqueName, duplicationFailedResult)
	}

	it should "throw a ValidationProcessorException when the operation name is NOT correct" in {
		val validAndUniqueName = "Valid and unique name"
		val incorrectOperationType = "incorrectOperation"
		(requestStub.getParameter _).when(PERIODICAL_OPERATION_TYPE_PARAM_ATTR_NAME).returns(incorrectOperationType)
		(periodicalServiceStub.findOneByName _).when(validAndUniqueName).returns(Some(Periodical()))

		assertThrows[ValidationProcessorException] {
			periodicalNameValidator.validate(paramValue = validAndUniqueName, request = requestStub)
		}
	}

	private def assertThatForPeriodicalNameReturns(name: String, validationResult: ValidationResult) = {
		assert(periodicalNameValidator.validate(paramValue = name, request = requestStub) == validationResult)
	}
}
