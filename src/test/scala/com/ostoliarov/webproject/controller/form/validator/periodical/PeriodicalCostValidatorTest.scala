package com.ostoliarov.webproject.controller.form.validator.periodical

import com.ostoliarov.webproject.FlatSpecScalaMockBase
import com.ostoliarov.webproject.controller.ApplicationResources.{MSG_PERIODICAL_COST_ERROR, MSG_SUCCESS, STATUS_CODE_SUCCESS, STATUS_CODE_VALIDATION_FAILED}
import com.ostoliarov.webproject.controller.form.validator.ValidationResult
import javax.servlet.http.HttpServletRequest

/**
	* Created by Oleg Stoliarov on 11/16/18.
	*/
class PeriodicalCostValidatorTest extends FlatSpecScalaMockBase {
	private val successResult = ValidationResult(
		statusCode = STATUS_CODE_SUCCESS,
		messageKey = MSG_SUCCESS
	)
	private val failedResult = ValidationResult(
		statusCode = STATUS_CODE_VALIDATION_FAILED,
		messageKey = MSG_PERIODICAL_COST_ERROR
	)
	private val requestMock = mock[HttpServletRequest]

	behavior of "PeriodicalCostValidator"

	it should "return the successful result for periodical cost = 0" in {
		assertThatForPeriodicalCostReturns("0", successResult)
	}

	it should "return the successful result for periodical cost = 10" in {
		assertThatForPeriodicalCostReturns("10", successResult)
	}

	it should "return the successful result for periodical cost = 100" in {
		assertThatForPeriodicalCostReturns("100", successResult)
	}

	it should "return the successful result for periodical cost = 99999" in {
		assertThatForPeriodicalCostReturns("99999", successResult)
	}

	it should "return a failed result for periodical cost = -1" in {
		assertThatForPeriodicalCostReturns("-1", failedResult)
	}

	it should "return a failed result for periodical cost = -10" in {
		assertThatForPeriodicalCostReturns("-10", failedResult)
	}

	it should "return a failed result for periodical cost = 100000" in {
		assertThatForPeriodicalCostReturns("100000", failedResult)
	}

	private def assertThatForPeriodicalCostReturns(cost: String, validationResult: ValidationResult) = {
		assert(PeriodicalCostValidator.validate(paramValue = cost, request = requestMock) == validationResult)
	}
}
