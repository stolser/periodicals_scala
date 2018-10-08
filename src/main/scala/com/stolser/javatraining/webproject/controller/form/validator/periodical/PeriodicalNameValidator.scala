package com.stolser.javatraining.webproject.controller.form.validator.periodical

import java.util.Objects.nonNull
import java.util.Optional
import java.util.regex.Pattern

import com.stolser.javatraining.webproject.controller.ApplicationResources._
import com.stolser.javatraining.webproject.controller.form.validator.{AbstractValidator, ValidationProcessorException, ValidationResult}
import com.stolser.javatraining.webproject.model.entity.periodical.Periodical
import com.stolser.javatraining.webproject.service.impl.PeriodicalServiceImpl
import javax.servlet.http.HttpServletRequest

/**
  * Created by Oleg Stoliarov on 10/8/18.
  */
object PeriodicalNameValidator extends AbstractValidator {
	private val INCORRECT_ENTITY_OPERATION_TYPE_DURING_VALIDATION = "Incorrect periodicalOperationType during validation!"
	private val periodicalService = PeriodicalServiceImpl.getInstance
	private val incorrectFailedResult = new ValidationResult(STATUS_CODE_VALIDATION_FAILED, MSG_PERIODICAL_NAME_INCORRECT)
	private val duplicationFailedResult = new ValidationResult(STATUS_CODE_VALIDATION_FAILED, MSG_PERIODICAL_NAME_DUPLICATION)

	override protected def checkParameter(periodicalName: String, request: HttpServletRequest): Optional[ValidationResult] = {
		if (nameDoesNotMatchRegex(periodicalName)) return Optional.of(incorrectFailedResult)
		if (isNameNotUnique(request, periodicalName)) return Optional.of(duplicationFailedResult)

		Optional.empty[ValidationResult]
	}

	private def nameDoesNotMatchRegex(periodicalName: String) = !Pattern.matches(PERIODICAL_NAME_PATTERN_REGEX, periodicalName)

	private def isNameNotUnique(request: HttpServletRequest, periodicalName: String) = {
		val periodicalId = java.lang.Long.parseLong(request.getParameter(ENTITY_ID_PARAM_NAME))
		val periodicalWithSuchNameInDb = periodicalService.findOneByName(periodicalName)
		val operationType =
			try
				Periodical.OperationType.valueOf(request.getParameter(PERIODICAL_OPERATION_TYPE_PARAM_ATTR_NAME).toUpperCase)
			catch {
				case e: IllegalArgumentException =>
					throw new ValidationProcessorException(INCORRECT_ENTITY_OPERATION_TYPE_DURING_VALIDATION, e)
			}

		def isOperationCreate(periodicalOperationType: Periodical.OperationType) =
			Periodical.OperationType.CREATE == periodicalOperationType

		def isOperationUpdate(periodicalOperationType: Periodical.OperationType) =
			Periodical.OperationType.UPDATE == periodicalOperationType

		/*
		 * if this is 'create' --> there must not be any periodical with the same name in the db;
		 * if this is 'update' --> we exclude this periodical from validation;
		 * Sorry for comments!
		 */

		nonNull(periodicalWithSuchNameInDb) && (isOperationCreate(operationType)
			|| (isOperationUpdate(operationType) && (periodicalId != periodicalWithSuchNameInDb.getId)))
	}
}
