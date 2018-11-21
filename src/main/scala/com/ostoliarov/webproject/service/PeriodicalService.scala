package com.ostoliarov.webproject.service

import com.ostoliarov.webproject.model.entity.periodical.{Periodical, PeriodicalStatus}
import com.ostoliarov.webproject.model.entity.statistics.PeriodicalNumberByCategory

/**
	* Created by Oleg Stoliarov on 10/15/18.
	*/
trait PeriodicalService {
	def findOneById(id: Long): Option[Periodical]

	def findOneByName(name: String): Option[Periodical]

	def findAll: List[Periodical]

	def findAllByStatus(status: PeriodicalStatus.Value): List[Periodical]

	/**
		* If the id of this periodical is 0, creates a new one. Otherwise tries to update
		* an existing periodical in the db with this id.
		* Use the returned instance for further operations as the save operation
		* might have changed the entity instance completely.
		*
		* @param periodical a periodical to be persisted. Must not be null
		* @return the saved periodical will never be null
		* @throws IllegalArgumentException - in case the given periodical is null
		*/
	@throws[IllegalArgumentException]
	def save(periodical: Periodical): Periodical

	/**
		* Updates a periodical and sets a new status 'discarded' only if there is no active subscriptions
		* of this periodical.
		*
		* @param periodical a periodical to be discarded. Must not be null
		* @return the number of affected rows: 0 - if the condition was not satisfied and updating
		*         has not happened; 1 - if the status of this periodical has been changed to 'discarded'
		* @throws IllegalArgumentException - in case the given periodical is null
		*/
	@throws[IllegalArgumentException]
	def updateAndSetDiscarded(periodical: Periodical): Int

	/**
		* Deletes from the db all periodicals with status = 'discarded'.
		*
		* @return the number of deleted periodicals
		*/
	def deleteAllDiscarded(): Int

	/**
		* Checks whether a periodical with the id has any active subscriptions.
		*
		* @return true if there are subscriptions with status = 'active' on the specified periodical
		*/
	def hasActiveSubscriptions(periodicalId: Long): Boolean

	def quantitativeStatistics: List[PeriodicalNumberByCategory]
}
