package com.stolser.javatraining.webproject.controller.request.processor

import DispatchException._
/**
  * Created by Oleg Stoliarov on 10/10/18.
  */
class DispatchException(message: String = null, cause: Throwable = null)
	extends RuntimeException(defaultMessage(message, cause), cause) {}

object DispatchException {
	def defaultMessage(message: String, cause: Throwable): String =
		if (message != null) message
		else if (cause != null) cause.toString
		else null
}
