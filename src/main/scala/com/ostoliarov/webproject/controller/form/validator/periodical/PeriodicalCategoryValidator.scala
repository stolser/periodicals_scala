package com.ostoliarov.webproject.controller.form.validator.periodical

import com.ostoliarov.webproject.controller.ApplicationResources.{MSG_PERIODICAL_CATEGORY_ERROR, STATUS_CODE_VALIDATION_FAILED}
import com.ostoliarov.webproject.controller.form.validator.{AbstractValidator, ValidationResult}
import com.ostoliarov.webproject.model.entity.periodical.PeriodicalCategory
import javax.servlet.http.HttpServletRequest

/**
	* Created by Oleg Stoliarov on 10/8/18.
	*/
object PeriodicalCategoryValidator extends AbstractValidator {
	private val failedResult = ValidationResult(
		statusCode = STATUS_CODE_VALIDATION_FAILED,
		messageKey = MSG_PERIODICAL_CATEGORY_ERROR
	)

	override protected def checkParameter(category: String,
																				request: HttpServletRequest): Option[ValidationResult] = {
		if (isCategoryNameNotValid(category)) return Some(failedResult)

		Option.empty[ValidationResult]
	}

	private def isCategoryNameNotValid(category: String) =
		!PeriodicalCategory.values
			.map(_.toString)
			.contains(category)
}
