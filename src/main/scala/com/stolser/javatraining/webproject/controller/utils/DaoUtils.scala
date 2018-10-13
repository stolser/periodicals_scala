package com.stolser.javatraining.webproject.controller.utils

import java.sql.{ResultSet, SQLException}

import com.stolser.javatraining.webproject.dao.impl.mysql.MysqlPeriodicalDao
import com.stolser.javatraining.webproject.model.entity.periodical.{Periodical, PeriodicalCategory}

/**
  * Created by Oleg Stoliarov on 10/13/18.
  */
object DaoUtils {
	/**
	  * Creates a new periodical using the data from the result set.
	  */
	@throws[SQLException]
	def getPeriodicalFromResultSet(rs: ResultSet): Periodical = {
		val periodicalBuilder = new Periodical.Builder
		periodicalBuilder
			.setId(rs.getLong(MysqlPeriodicalDao.DB_PERIODICALS_ID))
			.setName(rs.getString(MysqlPeriodicalDao.DB_PERIODICALS_NAME))
			.setCategory(PeriodicalCategory.valueOf(rs.getString(MysqlPeriodicalDao.DB_PERIODICALS_CATEGORY).toUpperCase))
			.setPublisher(rs.getString(MysqlPeriodicalDao.DB_PERIODICALS_PUBLISHER))
			.setDescription(rs.getString(MysqlPeriodicalDao.DB_PERIODICALS_DESCRIPTION))
			.setOneMonthCost(rs.getLong(MysqlPeriodicalDao.DB_PERIODICALS_ONE_MONTH_COST))
			.setStatus(Periodical.Status.valueOf(rs.getString(MysqlPeriodicalDao.DB_PERIODICALS_STATUS).toUpperCase))

		periodicalBuilder.build
	}
}
