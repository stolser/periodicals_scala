package com.stolser.javatraining.webproject.controller.form.validator

/**
	* Created by Oleg Stoliarov on 10/7/18.
	*
	* @param statusCode If equals to { @code STATUS_CODE_SUCCESS}, it means that validation has been passed successfully.
	*                   Otherwise - validation failed.
	* @param messageKey An i18n message key.
	*/
final case class ValidationResult private(statusCode: Int, messageKey: String) {}