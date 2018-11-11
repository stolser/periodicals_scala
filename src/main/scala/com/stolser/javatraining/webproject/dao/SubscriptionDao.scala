package com.stolser.javatraining.webproject.dao

import com.stolser.javatraining.webproject.model.entity.subscription.{Subscription, SubscriptionStatus}
import com.stolser.javatraining.webproject.model.entity.user.User

/**
	* Created by Oleg Stoliarov on 10/13/18.
	*/
trait SubscriptionDao extends GenericDao[Subscription] {
	def findOneByUserIdAndPeriodicalId(userId: Long,
																		 periodicalId: Long): Subscription

	/**
		* Retrieves all the subscriptions (active and expired) of the specified user.
		*/
	def findAllByUser(user: User): List[Subscription]

	def findAllByPeriodicalIdAndStatus(periodicalId: Long,
																		 status: SubscriptionStatus.Value): List[Subscription]
}
