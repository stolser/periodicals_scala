package com.stolser.javatraining.webproject.dao

import java.util
import java.util.List

import com.stolser.javatraining.webproject.model.entity.periodical.{Periodical, PeriodicalCategory}

/**
  * Created by Oleg Stoliarov on 10/13/18.
  */
trait PeriodicalDao extends GenericDao[Periodical] {
	def findOneByName(name: String): Periodical

	def findAllByStatus(status: Periodical.Status): util.List[Periodical]

	def findNumberOfPeriodicalsWithCategoryAndStatus(category: PeriodicalCategory, status: Periodical.Status): Int

	/**
	  * Updates a periodical and sets a new status 'discarded' only if there is no active subscriptions
	  * of this periodical.
	  *
	  * @return the number of affected rows: 0 - if the condition was not satisfied and updated
	  *         has not happened; 1 - if the status of this periodical has been changed to 'discarded'
	  */
	def updateAndSetDiscarded(periodical: Periodical): Int

	/**
	  * Deletes from the db all periodicals with status = 'discarded'.
	  *
	  * @return the number of deleted periodicals.
	  */
	def deleteAllDiscarded(): Int
}
