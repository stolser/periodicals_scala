package com.stolser.javatraining.webproject.model.entity.statistics

import com.stolser.javatraining.webproject.model.entity.periodical.PeriodicalCategory
import com.stolser.javatraining.webproject.model.entity.statistics.PeriodicalNumberByCategory._

import scala.beans.BeanProperty

/**
	* Created by Oleg Stoliarov on 10/19/18.
	* Represents quantitative statistics on existing periodicals divided by status.
	*/
case class PeriodicalNumberByCategory(@BeanProperty category: Option[PeriodicalCategory] = None,
																			@BeanProperty active: Int = 0,
																			@BeanProperty inActive: Int = 0,
																			@BeanProperty discarded: Int = 0) {
	require(active >= 0, negativeQuantityErrorMessage)
	require(inActive >= 0, negativeQuantityErrorMessage)
	require(discarded >= 0, negativeQuantityErrorMessage)

	def getCategoryAsPeriodicalCategory: PeriodicalCategory = category.orNull // used by JSP tags;
}

object PeriodicalNumberByCategory {
	private val negativeQuantityErrorMessage = "The quantity of periodicals cannot be negative."
}