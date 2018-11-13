package com.stolser.javatraining.webproject.controller.form.validator.periodical

import com.stolser.javatraining.webproject.controller.ApplicationResources.{MSG_PERIODICAL_CATEGORY_ERROR, STATUS_CODE_VALIDATION_FAILED}
import com.stolser.javatraining.webproject.controller.form.validator.{AbstractValidator, ValidationResult}
import com.stolser.javatraining.webproject.model.entity.periodical.PeriodicalCategory
import javax.servlet.http.HttpServletRequest

/**
  * Created by Oleg Stoliarov on 10/8/18.
  */
object PeriodicalCategoryValidator extends AbstractValidator {
	private val failedResult = new ValidationResult(STATUS_CODE_VALIDATION_FAILED, MSG_PERIODICAL_CATEGORY_ERROR)

	override protected def checkParameter(category: String, request: HttpServletRequest): Option[ValidationResult] =
		if (isCategoryNameCorrect(category)) Option.empty[ValidationResult]
		else Some(failedResult)

	private def isCategoryNameCorrect(category: String) =
		PeriodicalCategory.values
			.map(_.toString)
			.contains(category)
}
