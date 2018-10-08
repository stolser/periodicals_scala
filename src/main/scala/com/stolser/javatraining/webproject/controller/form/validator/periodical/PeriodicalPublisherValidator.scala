package com.stolser.javatraining.webproject.controller.form.validator.periodical

import java.util.Optional
import java.util.regex.Pattern

import com.stolser.javatraining.webproject.controller.ApplicationResources.{
	MSG_PERIODICAL_PUBLISHER_ERROR,
	PERIODICAL_PUBLISHER_PATTERN_REGEX,
	STATUS_CODE_VALIDATION_FAILED
}
import com.stolser.javatraining.webproject.controller.form.validator.{AbstractValidator, ValidationResult}
import javax.servlet.http.HttpServletRequest

/**
  * Created by Oleg Stoliarov on 10/8/18.
  */
object PeriodicalPublisherValidator extends AbstractValidator {
	private val failedResult = new ValidationResult(STATUS_CODE_VALIDATION_FAILED, MSG_PERIODICAL_PUBLISHER_ERROR)

	override protected def checkParameter(publisher: String, request: HttpServletRequest): Optional[ValidationResult] =
		if (isPublisherCorrect(publisher)) Optional.empty[ValidationResult]
		else Optional.of(failedResult)

	private def isPublisherCorrect(publisher: String) = Pattern.matches(PERIODICAL_PUBLISHER_PATTERN_REGEX, publisher)
}
