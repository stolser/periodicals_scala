package com.stolser.javatraining.webproject.service

import java.time.Instant
import java.util
import java.util.List

import com.stolser.javatraining.webproject.model.entity.invoice.Invoice
import com.stolser.javatraining.webproject.model.entity.statistics.FinancialStatistics

/**
  * Created by Oleg Stoliarov on 10/15/18.
  */
trait InvoiceService {
	def findOneById(invoiceId: Long): Invoice

	def findAllByUserId(userId: Long): util.List[Invoice]

	def findAllByPeriodicalId(periodicalId: Long): util.List[Invoice]

	def createNew(newInvoice: Invoice): Unit

	/**
	  * Updates the status of this invoice to { @code paid} and updates an existing subscription
	  * (or creates a new one) as one transaction.
	  *
	  * @param invoiceToPay invoice id to be paid
	  * @return true if after committing this invoice in the db has status 'PAID' and
	  *         false otherwise.
	  */
	def payInvoice(invoiceToPay: Invoice): Boolean

	def getFinStatistics(since: Instant, until: Instant): FinancialStatistics
}
