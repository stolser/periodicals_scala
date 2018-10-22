package com.stolser.javatraining.webproject.model.entity.subscription

import java.time.Instant

import com.google.common.base.Preconditions.checkNotNull
import com.stolser.javatraining.webproject.model.entity.periodical.Periodical
import com.stolser.javatraining.webproject.model.entity.user.User
import org.apache.commons.lang3.Validate
import org.apache.commons.lang3.Validate._

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
case class Subscription(@BeanProperty id: Long = 0,
						@BeanProperty user: User = User(),
						@BeanProperty periodical: Periodical = Periodical(),
						@BeanProperty deliveryAddress: String = "",
						@BeanProperty var endDate: Instant = null,
						@BeanProperty var status: SubscriptionStatus.Value = SubscriptionStatus.INACTIVE) {

	checkNotNull(user, "The user cannot be null. Use User() instead!": Any)
	checkNotNull(periodical, "The periodical cannot be null. Use Periodical() instead!": Any)
	checkNotNull(deliveryAddress)
	checkNotNull(status)

	override def toString: String = s"Subscription{id=$id, user='$user', periodical='$periodical', " +
		s"deliveryAddress='$deliveryAddress', endDate='$endDate', status='$status'}"
}

object SubscriptionStatus extends Enumeration {
	val ACTIVE, INACTIVE = Value
}

object Subscription {
	private[this] val emptySubscription = new Subscription()

	def apply: Subscription = emptySubscription
}