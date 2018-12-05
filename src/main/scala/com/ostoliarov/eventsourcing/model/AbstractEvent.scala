package com.ostoliarov.eventsourcing.model

import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

import com.ostoliarov.webproject.model.entity.invoice.Invoice
import com.ostoliarov.webproject.model.entity.periodical.Periodical
import com.ostoliarov.webproject.model.entity.user.User

/**
	* Created by Oleg Stoliarov on 12/5/18.
	*/
object AbstractEvent {
	private val nextEventUUID = new AtomicLong(1)
}

sealed abstract class AbstractEvent extends Event {
	this: Product =>

	import AbstractEvent._

	val uuid: Long = nextEventUUID.getAndIncrement()
	val time: Instant = Instant.now()

	override def eventMessage: String = s"Event{$uuid;$time;$userId;$productPrefix;$toString"
}

final case class SignInEvent(user: User, userIp: String) extends AbstractEvent {
	override val userId: Long = user.id

	override def toString: String = s"{user=$user,userIp=$userIp}"
}

final case class SignOutEvent(user: User) extends AbstractEvent {
	override val userId: Long = user.id

	override def toString: String = s"{user=$user}"
}

final case class CreateUserEvent(userId: Long, newUser: User) extends AbstractEvent {
	override def toString: String = s"{newUser=$newUser}"
}

final case class PersistOnePeriodicalEvent(userId: Long, periodical: Periodical) extends AbstractEvent {
	override def toString: String = s"{periodical=$periodical}"
}

final case class DeleteDiscardedPeriodicalsEvent(userId: Long, periodicals: Set[Periodical]) extends AbstractEvent {
	override def toString: String = s"{periodicals=$periodicals}"
}

final case class PersistOneInvoiceEvent(userId: Long, invoice: Invoice) extends AbstractEvent {
	override def toString: String = s"{invoice=$invoice}"
}

final case class PayOneInvoiceEvent(userId: Long, invoice: Invoice) extends AbstractEvent {
	override def toString: String = s"{invoice=$invoice}"
}
