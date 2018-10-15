package com.stolser.javatraining.webproject.service

import java.util
import java.util.List

import com.stolser.javatraining.webproject.model.entity.subscription.Subscription

/**
  * Created by Oleg Stoliarov on 10/15/18.
  */
trait SubscriptionService {
	def findAllByUserId(id: Long): util.List[Subscription]
}
