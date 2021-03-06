package com.ostoliarov.webproject.controller.form.validator

import com.ostoliarov.webproject.controller.ApplicationResources.{MSG_SUCCESS, STATUS_CODE_SUCCESS}
import javax.servlet.http.HttpServletRequest

/**
  * Created by Oleg Stoliarov on 10/7/18.
  */
abstract class AbstractValidator extends Validator {
	private lazy val successResult = ValidationResult(
		statusCode = STATUS_CODE_SUCCESS,
		messageKey = MSG_SUCCESS
	)

	override final def validate(paramValue: String, request: HttpServletRequest): ValidationResult =
		checkParameter(paramValue, request)
			.getOrElse(successResult)

	/**
	  * Returns an empty object if the parameter value is valid and an object describing the failure otherwise.
	  */
	protected def checkParameter(paramValue: String, request: HttpServletRequest): Option[ValidationResult]
}
