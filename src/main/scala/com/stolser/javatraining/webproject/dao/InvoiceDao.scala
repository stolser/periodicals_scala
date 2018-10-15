package com.stolser.javatraining.webproject.dao

import java.time.Instant
import java.util
import java.util.List

import com.stolser.javatraining.webproject.model.entity.invoice.Invoice

/**
  * Created by Oleg Stoliarov on 10/13/18.
  */
trait InvoiceDao extends GenericDao[Invoice] {
	def findAllByUserId(userId: Long): util.List[Invoice]

	def findAllByPeriodicalId(periodicalId: Long): util.List[Invoice]

	/**
	  * Returns the sum of all invoices that were created during the specified time period
	  * regardless whether they have been paid or not.
	  *
	  * @param since the beginning of the time period
	  * @param until the end of the time period
	  */
	def getCreatedInvoiceSumByCreationDate(since: Instant, until: Instant): Long

	/**
	  * Returns the sum of all invoices that were paid during the specified time period
	  * regardless when they were created.
	  *
	  * @param since the beginning of the time period
	  * @param until the end of the time period
	  */
	def getPaidInvoiceSumByPaymentDate(since: Instant, until: Instant): Long
}
