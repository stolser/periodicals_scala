package com.ostoliarov.webproject.dao

import com.ostoliarov.webproject.model.entity.periodical.{Periodical, PeriodicalCategory, PeriodicalStatus}

/**
	* Created by Oleg Stoliarov on 10/13/18.
	*/
trait PeriodicalDao extends GenericDao[Periodical] {
	def findOneByName(name: String): Option[Periodical]

	def findAllByStatus(status: PeriodicalStatus.Value): List[Periodical]

	def findNumberOfPeriodicalsWithCategoryAndStatus(category: PeriodicalCategory,
																									 status: PeriodicalStatus.Value): Int

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
