package com.stolser.javatraining.webproject.service.impl

import com.stolser.javatraining.webproject.model.entity.subscription.Subscription
import com.stolser.javatraining.webproject.service.ServiceUtils.withConnection
import com.stolser.javatraining.webproject.service.SubscriptionService

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
