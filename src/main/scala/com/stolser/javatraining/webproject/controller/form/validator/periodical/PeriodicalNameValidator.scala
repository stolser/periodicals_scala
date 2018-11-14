package com.stolser.javatraining.webproject.controller.form.validator.periodical

import com.stolser.javatraining.webproject.controller.ApplicationResources._
import com.stolser.javatraining.webproject.controller.form.validator.{AbstractValidator, ValidationProcessorException, ValidationResult}
import com.stolser.javatraining.webproject.model.entity.periodical.PeriodicalOperationType
import com.stolser.javatraining.webproject.service.impl.PeriodicalServiceImpl
import javax.servlet.http.HttpServletRequest

/**
	* Created by Oleg Stoliarov on 10/8/18.
	*/
object PeriodicalNameValidator extends AbstractValidator {
	private val INCORRECT_ENTITY_OPERATION_TYPE_DURING_VALIDATION = "Incorrect periodicalOperationType during validation!"
	private val periodicalService = PeriodicalServiceImpl
	private val incorrectFailedResult = new ValidationResult(STATUS_CODE_VALIDATION_FAILED, MSG_PERIODICAL_NAME_INCORRECT)
	private val duplicationFailedResult = new ValidationResult(STATUS_CODE_VALIDATION_FAILED, MSG_PERIODICAL_NAME_DUPLICATION)

	override protected def checkParameter(periodicalName: String,
																				request: HttpServletRequest): Option[ValidationResult] = {
		if (isNameNotValid(periodicalName)) return Some(incorrectFailedResult)
		if (isNameNotUnique(request, periodicalName)) return Some(duplicationFailedResult)

		Option.empty[ValidationResult]
	}

	private def isNameNotValid(periodicalName: String) = !periodicalName.matches(PERIODICAL_NAME_PATTERN_REGEX)

	private def isNameNotUnique(request: HttpServletRequest, periodicalName: String) = {
		val periodicalId = java.lang.Long.parseLong(request.getParameter(ENTITY_ID_PARAM_NAME))
		val operationType =
			try
				PeriodicalOperationType.withName(request.getParameter(PERIODICAL_OPERATION_TYPE_PARAM_ATTR_NAME).toUpperCase)
			catch {
				case e: IllegalArgumentException =>
					throw new ValidationProcessorException(INCORRECT_ENTITY_OPERATION_TYPE_DURING_VALIDATION, e)
			}

		def isOperationCreate(periodicalOperationType: PeriodicalOperationType.Value) =
			PeriodicalOperationType.CREATE == periodicalOperationType

		def isOperationUpdate(periodicalOperationType: PeriodicalOperationType.Value) =
			PeriodicalOperationType.UPDATE == periodicalOperationType

		/*
		 * if this is 'create' --> there must not be any periodical with the same name in the db;
		 * if this is 'update' --> we exclude this periodical from validation;
		 * Sorry for comments!
		 */
		periodicalService.findOneByName(periodicalName) match {
			case Some(periodicalWithSuchNameInDb) => (isOperationCreate(operationType)
				|| (isOperationUpdate(operationType) && (periodicalId != periodicalWithSuchNameInDb.id)))
			case None => false
		}
	}
}
