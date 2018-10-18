package com.stolser.javatraining.webproject.model.entity.invoice

import java.time.Instant

import com.stolser.javatraining.webproject.model.entity.periodical.Periodical
import com.stolser.javatraining.webproject.model.entity.user.User

import scala.beans.BeanProperty

/**
  * Created by Oleg Stoliarov on 10/15/18.
  */
@SerialVersionUID(-888L)
case class Invoice(
					  @BeanProperty id: Long = 0L,
					  @BeanProperty user: User = null,
					  @BeanProperty var periodical: Periodical = null,
					  @BeanProperty subscriptionPeriod: Int = 0,
					  @BeanProperty totalSum: Long = 0,
					  @BeanProperty creationDate: Instant = null,
					  @BeanProperty var paymentDate: Instant = null,
					  @BeanProperty var status: InvoiceStatus.Value = InvoiceStatus.NEW) extends Serializable {

	override def toString: String = s"Invoice_{id=$id, user=$user, periodical=$periodical, " +
		s"subscriptionPeriod=$subscriptionPeriod, totalSum=$totalSum, creationDate=$creationDate, " +
		s"paymentDate=$paymentDate, status=$status}"
}

object InvoiceStatus extends Enumeration {
	val NEW, PAID = Value
}

object Invoice {
	private[this] val emptyInvoice = new Invoice()

	def apply(): Invoice = emptyInvoice
}