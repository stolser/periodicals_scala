package com.stolser.javatraining.webproject.controller.form.validator.periodical

import com.stolser.javatraining.webproject.FlatSpecBase
import com.stolser.javatraining.webproject.controller.ApplicationResources.{MSG_SUCCESS, STATUS_CODE_SUCCESS}
import com.stolser.javatraining.webproject.controller.form.validator.ValidationResult
import javax.servlet.http.HttpServletRequest

/**
	* Created by Oleg Stoliarov on 11/15/18.
	*/
class PeriodicalCategoryValidatorTest extends FlatSpecBase {
	private lazy val successResult = ValidationResult(
		statusCode = STATUS_CODE_SUCCESS,
		messageKey = MSG_SUCCESS
	)
	private val requestMock = mock[HttpServletRequest]

	behavior of "PeriodicalCategoryValidator"

	it should "return the successful result" in {
		assert(PeriodicalCategoryValidator.validate(paramValue = "NEWS", request = requestMock) == successResult)
	}

}
