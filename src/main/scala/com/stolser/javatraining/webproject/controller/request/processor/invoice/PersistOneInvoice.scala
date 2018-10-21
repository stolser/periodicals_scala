package com.stolser.javatraining.webproject.controller.request.processor.invoice

import java.time.Instant
import java.util
import java.util.Objects.{isNull, nonNull}
import java.util.{ArrayList, List}

import com.stolser.javatraining.webproject.controller.ApplicationResources._
import com.stolser.javatraining.webproject.controller.message.{FrontMessageFactory, FrontendMessage}
import com.stolser.javatraining.webproject.controller.request.processor.RequestProcessor
import com.stolser.javatraining.webproject.controller.utils.HttpUtils
import com.stolser.javatraining.webproject.model.entity.invoice.{Invoice, InvoiceStatus}
import com.stolser.javatraining.webproject.model.entity.periodical.{Periodical, PeriodicalStatus}
import com.stolser.javatraining.webproject.model.entity.user.User
import com.stolser.javatraining.webproject.service.impl.{InvoiceServiceImpl, PeriodicalServiceImpl}
import com.stolser.javatraining.webproject.service.{InvoiceService, PeriodicalService}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.slf4j.{Logger, LoggerFactory}

/**
  * Created by Oleg Stoliarov on 10/10/18.
  * Processes a POST request to create a new invoice.
  */
object PersistOneInvoice extends RequestProcessor {
	private val LOGGER = LoggerFactory.getLogger(PersistOneInvoice.getClass)
	private val EXCEPTION_DURING_PERSISTING_INVOICE = "Exception during persisting an invoice: %s."
	private val periodicalService: PeriodicalService = PeriodicalServiceImpl
	private val invoiceService: InvoiceService = InvoiceServiceImpl
	private val messageFactory = FrontMessageFactory

	override def process(request: HttpServletRequest, response: HttpServletResponse): String = {
		val generalMessages: util.List[FrontendMessage] = new util.ArrayList[FrontendMessage]
		val periodicalId: Long = java.lang.Long.parseLong(request.getParameter(PERIODICAL_ID_PARAM_NAME))
		val periodicalInDb: Periodical = periodicalService.findOneById(periodicalId)

		if (isPeriodicalValid(periodicalInDb, request, generalMessages))
			tryToPersistNewInvoice(getNewInvoice(periodicalInDb, request), generalMessages)

		HttpUtils.addGeneralMessagesToSession(request, generalMessages)

		REDIRECT + getRedirectUri(periodicalId)
	}

	private def getRedirectUri(periodicalId: Long) = s"$PERIODICAL_LIST_URI/$periodicalId"

	private def periodicalExistsInDb(periodicalInDb: Periodical, generalMessages: util.List[FrontendMessage]) = {
		if (isNull(periodicalInDb)) generalMessages.add(messageFactory.getError(MSG_VALIDATION_PERIODICAL_IS_NULL))
		nonNull(periodicalInDb)
	}

	private def isPeriodicalVisible(periodicalInDb: Periodical, generalMessages: util.List[FrontendMessage]) = {
		val isVisible = PeriodicalStatus.ACTIVE == periodicalInDb.getStatus
		if (!isVisible) generalMessages.add(messageFactory.getError(MSG_VALIDATION_PERIODICAL_IS_NOT_VISIBLE))
		isVisible
	}

	private def isSubscriptionPeriodValid(request: HttpServletRequest, generalMessages: util.List[FrontendMessage]) = {
		val message = messageFactory.getError(MSG_VALIDATION_SUBSCRIPTION_PERIOD_IS_NOT_VALID)
		try {
			val subscriptionPeriod = request.getParameter(SUBSCRIPTION_PERIOD_PARAM_NAME).toInt
			if (!isPeriodValid(subscriptionPeriod)) generalMessages.add(message)
			isPeriodValid(subscriptionPeriod)
		} catch {
			case e: NumberFormatException =>
				generalMessages.add(message)
				false
		}
	}

	private def isPeriodValid(subscriptionPeriod: Int) = (subscriptionPeriod >= 1) && (subscriptionPeriod <= 12)

	private def isPeriodicalValid(periodicalInDb: Periodical,
								  request: HttpServletRequest,
								  generalMessages: util.List[FrontendMessage]) =
		periodicalExistsInDb(periodicalInDb, generalMessages) &&
			isPeriodicalVisible(periodicalInDb, generalMessages) &&
			isSubscriptionPeriodValid(request, generalMessages)

	private def tryToPersistNewInvoice(invoiceToPersist: Invoice, generalMessages: util.List[FrontendMessage]): Unit = {
		generalMessages.add(messageFactory.getInfo(MSG_VALIDATION_PASSED_SUCCESS))
		try {
			invoiceService.createNew(invoiceToPersist)
			generalMessages.add(messageFactory.getSuccess(MSG_INVOICE_CREATION_SUCCESS))
		} catch {
			case e: RuntimeException =>
				LOGGER.error(String.format(EXCEPTION_DURING_PERSISTING_INVOICE, invoiceToPersist), e)
				generalMessages.add(messageFactory.getError(MSG_INVOICE_PERSISTING_FAILED))
		}
	}

	private def getNewInvoice(periodicalInDb: Periodical, request: HttpServletRequest) = {
		val subscriptionPeriod = request.getParameter(SUBSCRIPTION_PERIOD_PARAM_NAME).toInt

		val totalSum = subscriptionPeriod * periodicalInDb.getOneMonthCost
		val userIdFromUri = HttpUtils.getFirstIdFromUri(request.getRequestURI)
		val user = User(id = userIdFromUri)

		Invoice(user = user,
			periodical = periodicalInDb,
			subscriptionPeriod = subscriptionPeriod,
			totalSum = totalSum,
			creationDate = Instant.now(),
			status = InvoiceStatus.NEW)
	}
}
