package com.stolser.javatraining.webproject.controller.form.validator.periodical

import com.stolser.javatraining.webproject.controller.ApplicationResources._
import com.stolser.javatraining.webproject.controller.form.validator.{AbstractValidator, ValidationProcessorException, ValidationResult}
import com.stolser.javatraining.webproject.model.entity.periodical.PeriodicalOperationType
import com.stolser.javatraining.webproject.service.PeriodicalService
import com.stolser.javatraining.webproject.service.impl.PeriodicalServiceImpl
import javax.servlet.http.HttpServletRequest

/**
	* Created by Oleg Stoliarov on 10/8/18.
	*/
object PeriodicalNameValidator extends AbstractValidator {
	private val INCORRECT_ENTITY_OPERATION_TYPE_DURING_VALIDATION = "Incorrect periodicalOperationType during validation!"
	private var periodicalServiceImpl = PeriodicalServiceImpl: PeriodicalService
	private val incorrectFailedResult = ValidationResult(
		statusCode = STATUS_CODE_VALIDATION_FAILED,
		messageKey = MSG_PERIODICAL_NAME_INCORRECT
	)
	private val duplicationFailedResult = ValidationResult(
		statusCode = STATUS_CODE_VALIDATION_FAILED,
		messageKey = MSG_PERIODICAL_NAME_DUPLICATION
	)

	override protected def checkParameter(periodicalName: String,
																				request: HttpServletRequest): Option[ValidationResult] = {
		if (isNameNotValid(periodicalName)) return Some(incorrectFailedResult)
		if (isNameNotUnique(request, periodicalName)) return Some(duplicationFailedResult)

		Option.empty[ValidationResult]
	}

	private def isNameNotValid(periodicalName: String) = !periodicalName.matches(PERIODICAL_NAME_PATTERN_REGEX)

	private def isNameNotUnique(request: HttpServletRequest,
															periodicalName: String) = {

		def isOperationCreate(periodicalOperationType: PeriodicalOperationType.Value) =
			PeriodicalOperationType.CREATE == periodicalOperationType

		def isOperationUpdate(periodicalOperationType: PeriodicalOperationType.Value) =
			PeriodicalOperationType.UPDATE == periodicalOperationType

		periodicalServiceImpl.findOneByName(periodicalName) match {
			case Some(periodicalWithSuchNameInDb) =>
				lazy val periodicalId = java.lang.Long.parseLong(request.getParameter(ENTITY_ID_PARAM_NAME))
				val operationType =
					try
						PeriodicalOperationType.withName(request.getParameter(PERIODICAL_OPERATION_TYPE_PARAM_ATTR_NAME).toUpperCase)
					catch {
						case e: NoSuchElementException =>
							throw ValidationProcessorException(INCORRECT_ENTITY_OPERATION_TYPE_DURING_VALIDATION, e)
					}

				/*
				* A periodical with the name from the request exists in the db. And the operation can be 'create' or 'update'.
		 		* If it's 'create' --> there must not be any periodical with the same name in the db.
		 		* If it's 'update' --> we exclude this periodical from validation.
		 		* Sorry for comments!
		 		*/
				(isOperationCreate(operationType)
					|| (isOperationUpdate(operationType) && (periodicalId != periodicalWithSuchNameInDb.id))
					)
			case None => false
		}
	}

	private[periodical] def periodicalService: PeriodicalService = periodicalServiceImpl

	private[periodical] def periodicalService_=(periodicalService: PeriodicalService): Unit = {
		require(periodicalService != null)

		this.periodicalServiceImpl = periodicalService
	}
}
