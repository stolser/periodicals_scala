package com.stolser.javatraining.webproject.dao

import java.util
import java.util.List

import com.stolser.javatraining.webproject.model.entity.subscription.Subscription
import com.stolser.javatraining.webproject.model.entity.user.User

/**
  * Created by Oleg Stoliarov on 10/13/18.
  */
trait SubscriptionDao extends GenericDao[Subscription] {
	def findOneByUserIdAndPeriodicalId(userId: Long, periodicalId: Long): Subscription

	/**
	  * Retrieves all the subscriptions (active and expired) of the specified user.
	  */
	def findAllByUser(user: User): util.List[Subscription]

	def findAllByPeriodicalIdAndStatus(periodicalId: Long, status: Subscription.Status): util.List[Subscription]
}
