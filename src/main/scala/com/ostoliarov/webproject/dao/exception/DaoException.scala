package com.ostoliarov.webproject.dao.exception

import com.ostoliarov.webproject.dao.exception.DaoException._

/**
	* Created by Oleg Stoliarov on 10/14/18.
	*/
case class DaoException private(message: String = null, cause: Throwable = null)
	extends RuntimeException(defaultMessage(message, cause), cause) {}

object DaoException {
	def defaultMessage(message: String, cause: Throwable): String =
		if (message != null) message
		else if (cause != null) cause.toString
		else null
}
