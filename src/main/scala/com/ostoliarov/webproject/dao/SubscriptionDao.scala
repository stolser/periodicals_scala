package com.ostoliarov.webproject.dao

import com.ostoliarov.webproject.model.entity.subscription.Subscription
import com.ostoliarov.webproject.model.entity.subscription.SubscriptionStatus.SubscriptionStatus
import com.ostoliarov.webproject.model.entity.user.User

/**
	* Created by Oleg Stoliarov on 10/13/18.
	*/
trait SubscriptionDao extends GenericDao[Subscription] {
	def findOneByUserIdAndPeriodicalId(userId: Long,
																		 periodicalId: Long): Option[Subscription]

	/**
		* Retrieves all the subscriptions (active and expired) of the specified user.
		*/
	def findAllByUser(user: User): List[Subscription]

	def findAllByPeriodicalIdAndStatus(periodicalId: Long,
																		 status: SubscriptionStatus): List[Subscription]
}
