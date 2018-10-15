package com.stolser.javatraining.webproject.dao

import java.sql.SQLException

import com.stolser.javatraining.webproject.dao.exception.DaoException

/**
  * Created by Oleg Stoliarov on 10/14/18.
  */
private[dao] object DaoUtils {
	def tryAndCatchSqlException[A](exceptionMessage: String = null)
								  (block: () => A): A = {
		try {
			block()
		} catch {
			case e: SQLException =>
				throw new DaoException(exceptionMessage, e)
		}
	}
}
