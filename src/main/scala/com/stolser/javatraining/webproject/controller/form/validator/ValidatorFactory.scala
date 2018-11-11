package com.stolser.javatraining.webproject.controller.form.validator

import com.stolser.javatraining.webproject.controller.ApplicationResources._
import com.stolser.javatraining.webproject.controller.form.validator.periodical.{PeriodicalCategoryValidator, PeriodicalCostValidator, PeriodicalNameValidator, PeriodicalPublisherValidator}
import com.stolser.javatraining.webproject.controller.form.validator.user.{UserEmailValidator, UserPasswordValidator}

/**
	* Created by Oleg Stoliarov on 10/7/18.
	* Produces validators for different parameter names.
	*/
object ValidatorFactory {
	private val THERE_IS_NO_VALIDATOR_FOR_SUCH_PARAM = "There is no validator for such a parameter!"

	def periodicalNameValidator: Validator = PeriodicalNameValidator

	def periodicalCategoryValidator: Validator = PeriodicalCategoryValidator

	def periodicalPublisherValidator: Validator = PeriodicalPublisherValidator

	def periodicalCostValidator: Validator = PeriodicalCostValidator

	def userPasswordValidator: Validator = UserPasswordValidator

	/**
		* Returns a concrete validator for this specific parameter.
		*
		* @param paramName a http parameter name that need to be validated
		*/
	def newValidator(paramName: String): Validator = paramName match {
		case PERIODICAL_NAME_PARAM_NAME =>
			PeriodicalNameValidator
		case PERIODICAL_CATEGORY_PARAM_NAME =>
			PeriodicalCategoryValidator
		case PERIODICAL_PUBLISHER_PARAM_NAME =>
			PeriodicalPublisherValidator
		case PERIODICAL_COST_PARAM_NAME =>
			PeriodicalCostValidator
		case USER_EMAIL_PARAM_NAME =>
			UserEmailValidator
		case USER_PASSWORD_PARAM_NAME =>
			UserPasswordValidator
		case _ =>
			throw new ValidationProcessorException(THERE_IS_NO_VALIDATOR_FOR_SUCH_PARAM)
	}
}
