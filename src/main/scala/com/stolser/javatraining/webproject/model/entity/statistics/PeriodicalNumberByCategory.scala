package com.stolser.javatraining.webproject.model.entity.statistics

import com.google.common.base.Preconditions
import com.google.common.base.Preconditions.checkNotNull
import com.stolser.javatraining.webproject.model.entity.periodical.PeriodicalCategory
import com.stolser.javatraining.webproject.model.entity.statistics.PeriodicalNumberByCategory._

import scala.beans.BeanProperty

/**
  * Created by Oleg Stoliarov on 10/19/18.
  * Represents quantitative statistics on existing periodicals divided by status.
  */
case class PeriodicalNumberByCategory(@BeanProperty category: PeriodicalCategory = null,
									  @BeanProperty active: Int = 0,
									  @BeanProperty inActive: Int = 0,
									  @BeanProperty discarded: Int = 0) {
	checkNotNull(category)
	require(active >= 0, negativeQuantityErrorMessage)
	require(inActive >= 0, negativeQuantityErrorMessage)
	require(discarded >= 0, negativeQuantityErrorMessage)
}

object PeriodicalNumberByCategory {
	private val negativeQuantityErrorMessage = "The quantity of periodicals cannot be negative."
}