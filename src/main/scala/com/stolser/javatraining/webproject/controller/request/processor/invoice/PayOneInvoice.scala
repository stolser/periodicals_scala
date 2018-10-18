package com.stolser.javatraining.webproject.controller.request.processor.invoice

import java.util
import java.util.Objects.{isNull, nonNull}
import java.util.{ArrayList, List}

import com.stolser.javatraining.webproject.controller.ApplicationResources._
import com.stolser.javatraining.webproject.controller.message.{FrontMessageFactory, FrontendMessage}
import com.stolser.javatraining.webproject.controller.request.processor.RequestProcessor
import com.stolser.javatraining.webproject.controller.utils.HttpUtils
import com.stolser.javatraining.webproject.model.entity.invoice.{Invoice, InvoiceStatus}
import com.stolser.javatraining.webproject.model.entity.periodical.Periodical
import com.stolser.javatraining.webproject.service.impl.{InvoiceServiceImpl, PeriodicalServiceImpl}
import com.stolser.javatraining.webproject.service.{InvoiceService, PeriodicalService}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.slf4j.{Logger, LoggerFactory}

/**
  * Created by Oleg Stoliarov on 10/10/18.
  * Processes a POST request to pay one invoice. In one transaction the status of the invoice is changed
  * to { @code paid} and a subscription is updated (created a new one or the status
  * and the end date are updated).
  */
object PayOneInvoice extends RequestProcessor {
	private val LOGGER = LoggerFactory.getLogger(PayOneInvoice.getClass)
	private val invoiceService: InvoiceService = InvoiceServiceImpl
	private val periodicalService: PeriodicalService = PeriodicalServiceImpl
	private val messageFactory = FrontMessageFactory

	override def process(request: HttpServletRequest, response: HttpServletResponse): String = {
		val generalMessages: util.List[FrontendMessage] = new util.ArrayList[FrontendMessage]
		val invoiceInDb: Invoice = invoiceService.findOneById(getInvoiceIdFromRequest(request).toLong)

		if (isInvoiceValid(invoiceInDb, generalMessages)) tryToPayInvoice(invoiceInDb, request, generalMessages)

		HttpUtils.addGeneralMessagesToSession(request, generalMessages)

		REDIRECT + CURRENT_USER_ACCOUNT_URI
	}

	private def getInvoiceIdFromRequest(request: HttpServletRequest) =
		HttpUtils.getFirstIdFromUri(request.getRequestURI.replaceFirst("/backend/users/\\d+/", ""))

	private def isInvoiceValid(invoiceInDb: Invoice, generalMessages: util.List[FrontendMessage]) =
		invoiceExistsInDb(invoiceInDb, generalMessages) &&
			isInvoiceNew(invoiceInDb, generalMessages) &&
			isPeriodicalVisible(invoiceInDb, generalMessages)

	private def invoiceExistsInDb(invoiceInDb: Invoice, generalMessages: util.List[FrontendMessage]) = {
		if (isNull(invoiceInDb)) generalMessages.add(messageFactory.getError(MSG_VALIDATION_NO_SUCH_INVOICE))
		nonNull(invoiceInDb)
	}

	private def isInvoiceNew(invoiceInDb: Invoice, generalMessages: util.List[FrontendMessage]) = {
		val isNew = InvoiceStatus.NEW == invoiceInDb.status
		if (!isNew) generalMessages.add(messageFactory.getError(MSG_VALIDATION_INVOICE_IS_NOT_NEW))
		isNew
	}

	private def isPeriodicalVisible(invoiceInDb: Invoice, generalMessages: util.List[FrontendMessage]) = {
		val isPeriodicalInDbActive = isPeriodicalActive(invoiceInDb)
		if (!isPeriodicalInDbActive) generalMessages.add(messageFactory.getError(MSG_VALIDATION_PERIODICAL_IS_NOT_VISIBLE))
		isPeriodicalInDbActive
	}

	private def tryToPayInvoice(invoiceInDb: Invoice, request: HttpServletRequest, generalMessages: util.List[FrontendMessage]): Unit = {
		try {
			generalMessages.add(messageFactory.getInfo(MSG_VALIDATION_PASSED_SUCCESS))
			val isInvoicePaid = invoiceService.payInvoice(invoiceInDb)
			val resultMessage = if (isInvoicePaid) MSG_INVOICE_PAYMENT_SUCCESS else MSG_INVOICE_PAYMENT_ERROR

			generalMessages.add(messageFactory.getSuccess(resultMessage))
		} catch {
			case e: RuntimeException =>
				LOGGER.error(s"User id = ${HttpUtils.getUserIdFromSession(request)}. Exception during paying invoice $invoiceInDb.", e)
				generalMessages.add(messageFactory.getError(MSG_INVOICE_PAYMENT_ERROR))
		}
	}

	private def isPeriodicalActive(invoiceInDb: Invoice) = {
		val periodicalInDb = periodicalService.findOneById(getPeriodicalIdFromInvoice(invoiceInDb))
		Periodical.Status.ACTIVE == periodicalInDb.getStatus
	}

	private def getPeriodicalIdFromInvoice(invoiceInDb: Invoice) = invoiceInDb.periodical.getId
}
