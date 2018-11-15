package com.stolser.javatraining.webproject.controller.form.validator.periodical

import com.stolser.javatraining.webproject.controller.ApplicationResources.{MSG_PERIODICAL_COST_ERROR, PERIODICAL_COST_PATTERN_REGEX, STATUS_CODE_VALIDATION_FAILED}
import com.stolser.javatraining.webproject.controller.form.validator.{AbstractValidator, ValidationResult}
import javax.servlet.http.HttpServletRequest

/**
	* Created by Oleg Stoliarov on 10/8/18.
	*/
object PeriodicalCostValidator extends AbstractValidator {
	private val failedResult = new ValidationResult(
		statusCode = STATUS_CODE_VALIDATION_FAILED,
		messageKey = MSG_PERIODICAL_COST_ERROR
	)

	override protected def checkParameter(periodicalCost: String,
																				request: HttpServletRequest): Option[ValidationResult] = {
		if (isCostNotValid(periodicalCost)) return Some(failedResult)

		Option.empty[ValidationResult]
	}

	private def isCostNotValid(periodicalCost: String) = !periodicalCost.matches(PERIODICAL_COST_PATTERN_REGEX)
}
