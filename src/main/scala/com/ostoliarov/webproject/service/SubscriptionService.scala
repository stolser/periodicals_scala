package com.ostoliarov.webproject.service

import com.ostoliarov.webproject.model.entity.subscription.Subscription

/**
	* Created by Oleg Stoliarov on 10/15/18.
	*/
trait SubscriptionService {
	def findAllByUserId(id: Long): List[Subscription]
}
