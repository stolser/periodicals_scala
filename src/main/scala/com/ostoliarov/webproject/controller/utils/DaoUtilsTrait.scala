package com.ostoliarov.webproject.controller.utils

import java.sql.{ResultSet, SQLException}

import com.ostoliarov.webproject.model.entity.periodical.Periodical

/**
	* Created by Oleg Stoliarov on 11/6/18.
	*/
trait DaoUtilsTrait {
	@throws[SQLException]
	def periodicalFromResultSet(rs: ResultSet): Periodical
}
