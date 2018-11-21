package com.stolser.javatraining.webproject.controller.request.processor.invoice

import com.stolser.javatraining.webproject.controller.ApplicationResources._
import com.stolser.javatraining.webproject.controller.message._
import com.stolser.javatraining.webproject.controller.request.processor.RequestProcessor
import com.stolser.javatraining.webproject.controller.utils.HttpUtils
import com.stolser.javatraining.webproject.model.entity.invoice.{Invoice, InvoiceStatus}
import com.stolser.javatraining.webproject.model.entity.periodical.PeriodicalStatus
import com.stolser.javatraining.webproject.service.impl.mysql.{InvoiceServiceMysqlImpl, PeriodicalServiceMysqlImpl}
import com.stolser.javatraining.webproject.service.{InvoiceService, PeriodicalService}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.slf4j.LoggerFactory

import scala.collection.mutable

/**
	* Created by Oleg Stoliarov on 10/10/18.
	* Processes a POST request to pay one invoice. In one transaction the status of the invoice is changed
	* to { @code paid} and a subscription is updated (created a new one or the status
	* and the end date are updated).
	*/
object PayOneInvoice extends RequestProcessor {
	private val LOGGER = LoggerFactory.getLogger(PayOneInvoice.getClass)
	private val invoiceService: InvoiceService = InvoiceServiceMysqlImpl
	private val periodicalService: PeriodicalService = PeriodicalServiceMysqlImpl
	private val messageFactory = FrontMessageFactory

	override def process(request: HttpServletRequest,
											 response: HttpServletResponse): String = {
		val generalMessages = mutable.ListBuffer[FrontendMessage]()
		val invoiceIdFromRequest = HttpUtils.firstIdFromUri(request.getRequestURI.replaceFirst("/backend/users/\\d+/", ""))
		val invoiceInDb = invoiceService.findOneById(invoiceIdFromRequest.toLong)

		invoiceInDb match {
			case Some(invoice) if isInvoiceValid(invoice, generalMessages) =>
				tryToPayInvoice(invoice, request, generalMessages)
			case None =>
				generalMessages += messageFactory.error(MSG_VALIDATION_NO_SUCH_INVOICE)
		}

		HttpUtils.addGeneralMessagesToSession(request, generalMessages)

		REDIRECT + CURRENT_USER_ACCOUNT_URI
	}

	private def isInvoiceValid(invoiceInDb: Invoice,
														 generalMessages: mutable.ListBuffer[FrontendMessage]): Boolean = {

		def isInvoiceNew() =
			if (InvoiceStatus.NEW != invoiceInDb.status) {
				generalMessages += messageFactory.error(MSG_VALIDATION_INVOICE_IS_NOT_NEW)
				false
			} else true

		val isPeriodicalActive =
			periodicalService.findOneById(invoiceInDb.periodical.id) match {
				case Some(periodicalInDb) =>
					PeriodicalStatus.ACTIVE == periodicalInDb.status
				case None =>
					throw new RuntimeException(s"Periodical corresponding to invoice = $invoiceInDb does not exist, but must.")
			}

		def isPeriodicalVisible() =
			if (!isPeriodicalActive) {
				generalMessages += messageFactory.error(MSG_VALIDATION_PERIODICAL_IS_NOT_VISIBLE)
				false
			} else true

		isInvoiceNew() && isPeriodicalVisible()
	}

	private def tryToPayInvoice(invoiceInDb: Invoice,
															request: HttpServletRequest,
															generalMessages: mutable.ListBuffer[FrontendMessage]): Unit = {
		try {
			generalMessages += messageFactory.info(MSG_VALIDATION_PASSED_SUCCESS)
			val isInvoicePaid = invoiceService.payInvoice(invoiceInDb)
			val resultMessage = if (isInvoicePaid) MSG_INVOICE_PAYMENT_SUCCESS else MSG_INVOICE_PAYMENT_ERROR

			generalMessages += messageFactory.success(resultMessage)
		} catch {
			case e: RuntimeException =>
				LOGGER.error(s"User id = ${HttpUtils.userIdFromSession(request)}. Exception during paying invoice $invoiceInDb.", e)
				generalMessages += messageFactory.error(MSG_INVOICE_PAYMENT_ERROR)
		}
	}
}
