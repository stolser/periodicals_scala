package com.ostoliarov.webproject

import java.sql.{PreparedStatement, ResultSet, SQLException}

import com.ostoliarov.webproject.dao.exception.DaoException

/**
	* Created by Oleg Stoliarov on 12/8/18.
	*/
package object dao {
	@throws[SQLException]
	def tryCreateNewEntityAndRetrieveGeneratedId(st: PreparedStatement,
																							 exceptionMessage: String): Long = {
		st.executeUpdate match {
			case 0 => throw DaoException(exceptionMessage) // it's insertion into the db, so it must return row count > 0;
			case _ => withResources(st.getGeneratedKeys) {
				generatedKeys: ResultSet => {
					generatedKeys.next
					generatedKeys.getLong(1)
				}
			}
		}
	}
}
