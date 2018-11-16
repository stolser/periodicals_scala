package com.stolser.javatraining.webproject.controller.form.validator

import com.stolser.javatraining.webproject.controller.form.validator.ValidationProcessorException._

/**
  * Created by Oleg Stoliarov on 10/7/18.
  */
case class ValidationProcessorException private(message: String = null, cause: Throwable = null)
	extends RuntimeException(defaultMessage(message, cause), cause) {}

object ValidationProcessorException {
	private def defaultMessage(message: String, cause: Throwable): String =
		if (message != null) message
		else if (cause != null) cause.toString
		else null
}
