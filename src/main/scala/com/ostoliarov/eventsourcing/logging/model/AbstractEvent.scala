package com.ostoliarov.eventsourcing.logging.model

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

final case class SignInEvent(userId: Long, userIp: String) extends AbstractEvent {
	override def toString: String = s"{userIp=$userIp}"
}

final case class SignOutEvent(userId: Long) extends AbstractEvent {
	override def toString: String = ""
}

final case class CreateUserEvent(userId: Long, newUser: User) extends AbstractEvent {
	override def toString: String = s"{newUser=$newUser}"
}

final case class CreatePeriodicalEvent(userId: Long, newPeriodical: Periodical) extends AbstractEvent {
	override def toString: String = s"{periodical=$newPeriodical}"
}

final case class UpdatePeriodicalEvent(userId: Long, updatedPeriodical: Periodical) extends AbstractEvent {
	override def toString: String = s"{periodical=$updatedPeriodical}"
}

final case class DeleteDiscardedPeriodicalsEvent(userId: Long) extends AbstractEvent {
	override def toString: String = ""
}

final case class PersistOneInvoiceEvent(userId: Long, invoice: Invoice) extends AbstractEvent {
	override def toString: String = s"{invoice=$invoice}"
}

final case class PayOneInvoiceEvent(userId: Long, invoiceId: Long) extends AbstractEvent {
	override def toString: String = s"{invoiceId=$invoiceId}"
}
