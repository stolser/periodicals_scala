package com.ostoliarov.webproject.model.entity.subscription

import java.time.Instant

import com.google.common.base.Preconditions.checkNotNull
import com.ostoliarov.webproject.model.entity.periodical.Periodical
import com.ostoliarov.webproject.model.entity.subscription.SubscriptionStatus.SubscriptionStatus
import com.ostoliarov.webproject.model.entity.user.User

import scala.beans.BeanProperty

/**
	* Created by Oleg Stoliarov on 10/20/18.
	*
	* @param user       The user this subscription belongs to.
	* @param periodical The periodical this subscription is on.
	* @param endDate    The expiration date of this subscription. It can be prolonged by creating and paying a new invoice
	*                   for the same periodical.
	* @param status     Is { @code active} when a subscription is created. Becomes { @code inactive} when
	*                   this subscription is expired.
	*/
case class Subscription private(@BeanProperty id: Long = 0,
																@BeanProperty user: User = User(),
																@BeanProperty periodical: Periodical = Periodical(),
																@BeanProperty deliveryAddress: String = "",
																@BeanProperty var endDate: Option[Instant] = None,
																@BeanProperty var status: SubscriptionStatus = SubscriptionStatus.INACTIVE) {

	require(id >= 0)
	checkNotNull(user, "The user cannot be null. Use User() instead!": Any)
	checkNotNull(periodical, "The periodical cannot be null. Use Periodical() instead!": Any)
	checkNotNull(deliveryAddress)
	checkNotNull(status)

	override def toString: String = s"Subscription{id=$id, user='$user', periodical='$periodical', " +
		s"deliveryAddress='$deliveryAddress', endDate='$endDate', status='$status'}"

	@BeanProperty val endDateAsInstant: Instant = endDate.orNull // used by JSP tags;
}

object Subscription {
	private[this] val emptySubscription = new Subscription()

	def apply: Subscription = emptySubscription
}