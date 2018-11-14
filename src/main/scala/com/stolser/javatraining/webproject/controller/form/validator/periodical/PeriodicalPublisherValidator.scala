package com.stolser.javatraining.webproject.controller.form.validator.periodical

import com.stolser.javatraining.webproject.controller.ApplicationResources.{MSG_PERIODICAL_PUBLISHER_ERROR, PERIODICAL_PUBLISHER_PATTERN_REGEX, STATUS_CODE_VALIDATION_FAILED}
import com.stolser.javatraining.webproject.controller.form.validator.{AbstractValidator, ValidationResult}
import javax.servlet.http.HttpServletRequest

/**
	* Created by Oleg Stoliarov on 10/8/18.
	*/
object PeriodicalPublisherValidator extends AbstractValidator {
	private val failedResult = new ValidationResult(STATUS_CODE_VALIDATION_FAILED, MSG_PERIODICAL_PUBLISHER_ERROR)

	override protected def checkParameter(publisher: String,
																				request: HttpServletRequest): Option[ValidationResult] = {
		if (isPublisherNameNotValid(publisher)) return Some(failedResult)

		Option.empty[ValidationResult]
	}

	private def isPublisherNameNotValid(publisher: String) = !publisher.matches(PERIODICAL_PUBLISHER_PATTERN_REGEX)
}
