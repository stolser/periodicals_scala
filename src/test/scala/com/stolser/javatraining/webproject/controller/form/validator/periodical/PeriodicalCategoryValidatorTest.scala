package com.stolser.javatraining.webproject.controller.form.validator.periodical

import com.stolser.javatraining.webproject.FlatSpecScalaMockBase
import com.stolser.javatraining.webproject.controller.ApplicationResources.{MSG_PERIODICAL_CATEGORY_ERROR, MSG_SUCCESS, STATUS_CODE_SUCCESS, STATUS_CODE_VALIDATION_FAILED}
import com.stolser.javatraining.webproject.controller.form.validator.ValidationResult
import javax.servlet.http.HttpServletRequest

/**
	* Created by Oleg Stoliarov on 11/15/18.
	*/
class PeriodicalCategoryValidatorTest extends FlatSpecScalaMockBase {
	private val successResult = ValidationResult(
		statusCode = STATUS_CODE_SUCCESS,
		messageKey = MSG_SUCCESS
	)
	val failedResult = ValidationResult(
		statusCode = STATUS_CODE_VALIDATION_FAILED,
		messageKey = MSG_PERIODICAL_CATEGORY_ERROR
	)
	private val requestMock = mock[HttpServletRequest]

	behavior of "PeriodicalCategoryValidator"

	it should "return the successful result for every correct category name" in {
		val periodicalCategoryNames = Set("NEWS", "NATURE", "FITNESS", "BUSINESS",
			"SPORTS", "SCIENCE_AND_ENGINEERING", "TRAVELLING")

		periodicalCategoryNames.foreach(_ =>
			assertThatForCategoryNameReturns(categoryName = "NEWS", validationResult = successResult)
		)
	}

	it should "return a failed result for the 'news' category name" in {
		assertThatForCategoryNameReturns(categoryName = "news", validationResult = failedResult)
	}

	it should "return a failed result for the 'notExisting' category name" in {
		assertThatForCategoryNameReturns(categoryName = "notExisting", validationResult = failedResult)
	}

	private def assertThatForCategoryNameReturns(categoryName: String, validationResult: ValidationResult) = {
		assert(PeriodicalCategoryValidator.validate(paramValue = categoryName, request = requestMock) == validationResult)
	}
}
