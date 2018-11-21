package com.ostoliarov.webproject.service.impl

import com.ostoliarov.webproject.model.entity.subscription.Subscription
import com.ostoliarov.webproject.service.{SubscriptionService, _}

/**
	* Created by Oleg Stoliarov on 11/21/18.
	*/
abstract class SubscriptionServiceImpl extends SubscriptionService {
	this: ServiceDependency =>

	override def findAllByUserId(id: Long): List[Subscription] =
		withConnection { conn =>
			daoFactory.userDao(conn).findOneById(id) match {
				case Some(user) => daoFactory.subscriptionDao(conn).findAllByUser(user)
				case None => List.empty[Subscription]
			}
		}
}
