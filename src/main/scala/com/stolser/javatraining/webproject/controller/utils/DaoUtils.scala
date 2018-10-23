package com.stolser.javatraining.webproject.controller.utils

import java.sql.{ResultSet, SQLException}

import com.stolser.javatraining.webproject.dao.impl.mysql.MysqlPeriodicalDao._
import com.stolser.javatraining.webproject.model.entity.periodical.{Periodical, PeriodicalCategory, PeriodicalStatus}

/**
  * Created by Oleg Stoliarov on 10/13/18.
  */
object DaoUtils {
	/**
	  * Creates a new periodical using the data from the result set.
	  */
	@throws[SQLException]
	def getPeriodicalFromResultSet(rs: ResultSet): Periodical =
		Periodical(
			id = rs.getLong(DB_PERIODICALS_ID),
			name = rs.getString(DB_PERIODICALS_NAME),
			category = PeriodicalCategory.withName(rs.getString(DB_PERIODICALS_CATEGORY).toUpperCase),
			publisher = rs.getString(DB_PERIODICALS_PUBLISHER),
			description = Option(rs.getString(DB_PERIODICALS_DESCRIPTION)),
			oneMonthCost = rs.getLong(DB_PERIODICALS_ONE_MONTH_COST),
			status = PeriodicalStatus.withName(rs.getString(DB_PERIODICALS_STATUS).toUpperCase)
		)
}
