package com.stolser.javatraining.webproject.controller.form.validator.periodical

import java.util.Optional
import java.util.regex.Pattern

import com.stolser.javatraining.webproject.controller.ApplicationResources.{
	MSG_PERIODICAL_COST_ERROR,
	STATUS_CODE_VALIDATION_FAILED,
	PERIODICAL_COST_PATTERN_REGEX
}
import com.stolser.javatraining.webproject.controller.form.validator.{AbstractValidator, ValidationResult}
import javax.servlet.http.HttpServletRequest

/**
  * Created by Oleg Stoliarov on 10/8/18.
  */
object PeriodicalCostValidator extends AbstractValidator {
	private val failedResult = new ValidationResult(STATUS_CODE_VALIDATION_FAILED, MSG_PERIODICAL_COST_ERROR)

	override protected def checkParameter(periodicalCost: String, request: HttpServletRequest): Optional[ValidationResult] =
		if (isCostCorrect(periodicalCost)) Optional.empty[ValidationResult]
		else Optional.of(failedResult)

	private def isCostCorrect(periodicalCost: String) = Pattern.matches(PERIODICAL_COST_PATTERN_REGEX, periodicalCost)
}