package com.ostoliarov.webproject.model.entity.invoice

import java.time.Instant

import com.google.common.base.Preconditions._
import com.ostoliarov.webproject.model.entity.invoice.InvoiceStatus.InvoiceStatus
import com.ostoliarov.webproject.model.entity.periodical.Periodical
import com.ostoliarov.webproject.model.entity.user.User

import scala.beans.BeanProperty

/**
	* Created by Oleg Stoliarov on 10/15/18.
	*/
case class Invoice private(@BeanProperty id: Long = 0L,
													 @BeanProperty user: User = User(),
													 @BeanProperty var periodical: Periodical = Periodical(),
													 @BeanProperty subscriptionPeriod: Int = 0,
													 @BeanProperty totalSum: Long = 0,
													 @BeanProperty creationDate: Option[Instant] = None,
													 @BeanProperty var paymentDate: Option[Instant] = None,
													 @BeanProperty var status: InvoiceStatus = InvoiceStatus.NEW) {

	require(id >= 0)
	require((0 to 12).contains(subscriptionPeriod))
	require(totalSum >= 0)
	checkNotNull(user, "The user cannot be null. Use User() instead!": Any)
	checkNotNull(periodical, "The periodical cannot be null. Use Periodical() instead!": Any)
	checkNotNull(status)

	override def toString: String = s"Invoice_{id=$id, user=$user, periodical=$periodical, " +
		s"subscriptionPeriod=$subscriptionPeriod, totalSum=$totalSum, creationDate=$creationDate, " +
		s"paymentDate=$paymentDate, status=$status}"

	@BeanProperty val creationDateAsInstant: Instant = creationDate.orNull // used by JSP tags;
	@BeanProperty val paymentDateAsInstant: Instant = paymentDate.orNull // used by JSP tags;
}

object Invoice {
	private[this] val emptyInvoice = new Invoice()

	def apply: Invoice = emptyInvoice
}