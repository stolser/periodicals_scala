package com.stolser.javatraining.webproject.controller.utils

import java.sql.{ResultSet, SQLException}

import com.stolser.javatraining.webproject.model.entity.periodical.Periodical

/**
	* Created by Oleg Stoliarov on 11/6/18.
	*/
trait DaoUtilsTrait {
	@throws[SQLException]
	def getPeriodicalFromResultSet(rs: ResultSet): Periodical
}
