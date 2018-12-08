package com.ostoliarov.webproject.controller.request.processor.invoice

import java.time.Instant

import com.ostoliarov.eventsourcing.logging.EventLoggingUtils
import com.ostoliarov.eventsourcing.logging.model.PersistOneInvoiceEvent
import com.ostoliarov.webproject.controller.ApplicationResources._
import com.ostoliarov.webproject.controller.message.{FrontMessageFactory, FrontendMessage}
import com.ostoliarov.webproject.controller.request.processor.DispatchType._
import com.ostoliarov.webproject.controller.request.processor.{AbstractViewName, RequestProcessor, ResourceRequest}
import com.ostoliarov.webproject.controller.utils.HttpUtils._
import com.ostoliarov.webproject.model.entity.invoice.{Invoice, InvoiceStatus}
import com.ostoliarov.webproject.model.entity.periodical.{Periodical, PeriodicalStatus}
import com.ostoliarov.webproject.model.entity.user.User
import com.ostoliarov.webproject.service.impl.mysql.{InvoiceServiceMysqlImpl, PeriodicalServiceMysqlImpl}
import com.ostoliarov.webproject.service.{InvoiceService, PeriodicalService}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.slf4j.LoggerFactory

import scala.collection.mutable

/**
	* Created by Oleg Stoliarov on 10/10/18.
	* Processes a POST request to create a new invoice.
	*/
object PersistOneInvoice extends RequestProcessor {
	private val LOGGER = LoggerFactory.getLogger(PersistOneInvoice.getClass)
	private val EXCEPTION_DURING_PERSISTING_INVOICE = "Exception during persisting an invoice: %s."
	private val periodicalService: PeriodicalService = PeriodicalServiceMysqlImpl
	private val invoiceService: InvoiceService = InvoiceServiceMysqlImpl
	private val messageFactory = FrontMessageFactory

	override def process(request: HttpServletRequest,
											 response: HttpServletResponse): ResourceRequest = {
		val generalMessages = mutable.ListBuffer[FrontendMessage]()
		val periodicalId = java.lang.Long.parseLong(request.getParameter(PERIODICAL_ID_PARAM_NAME))
		val periodicalInDb = periodicalService.findOneById(periodicalId)

		periodicalInDb match {
			case Some(periodical) if isPeriodicalValid(periodical, request, generalMessages) =>
				tryToPersistInvoice(newInvoice(periodical, request), generalMessages, request)
			case None =>
				generalMessages += messageFactory.error(MSG_VALIDATION_PERIODICAL_IS_NULL)
		}

		addGeneralMessagesToSession(request, generalMessages)

		ResourceRequest(REDIRECT, AbstractViewName(redirectUri(periodicalId)))
	}

	private def isPeriodicalValid(periodicalInDb: Periodical,
																request: HttpServletRequest,
																generalMessages: mutable.ListBuffer[FrontendMessage]) =
		(isPeriodicalVisible(periodicalInDb, generalMessages)
			&& isSubscriptionPeriodValid(request, generalMessages))

	private def isPeriodicalVisible(periodicalInDb: Periodical,
																	generalMessages: mutable.ListBuffer[FrontendMessage]) =
		if (PeriodicalStatus.ACTIVE != periodicalInDb.status) {
			generalMessages += messageFactory.error(MSG_VALIDATION_PERIODICAL_IS_NOT_VISIBLE)
			false
		} else true

	private def isSubscriptionPeriodValid(request: HttpServletRequest,
																				generalMessages: mutable.ListBuffer[FrontendMessage]) = {
		val errorMessage = messageFactory.error(MSG_VALIDATION_SUBSCRIPTION_PERIOD_IS_NOT_VALID)
		try {
			val subscriptionPeriod = request.getParameter(SUBSCRIPTION_PERIOD_PARAM_NAME).toInt
			val isPeriodValid = (1 to 12).contains(subscriptionPeriod)

			if (!isPeriodValid)
				generalMessages += errorMessage

			isPeriodValid
		} catch {
			case _: NumberFormatException =>
				generalMessages += errorMessage
				false
		}
	}

	private def newInvoice(periodicalInDb: Periodical,
												 request: HttpServletRequest) = {
		val subscriptionPeriod = request.getParameter(SUBSCRIPTION_PERIOD_PARAM_NAME).toInt

		val totalSum = subscriptionPeriod * periodicalInDb.oneMonthCost
		val userIdFromUri = firstIdFromUri(request.getRequestURI)
		val user = User(id = userIdFromUri)

		Invoice(
			user = user,
			periodical = periodicalInDb,
			subscriptionPeriod = subscriptionPeriod,
			totalSum = totalSum,
			creationDate = Some(Instant.now()),
			status = InvoiceStatus.NEW
		)
	}

	private def tryToPersistInvoice(invoiceToPersist: Invoice,
																		 generalMessages: mutable.ListBuffer[FrontendMessage],
																		 request: HttpServletRequest): Unit = {
		generalMessages += messageFactory.info(MSG_VALIDATION_PASSED_SUCCESS)
		try {
			invoiceService.createNew(invoiceToPersist)
			generalMessages += messageFactory.success(MSG_INVOICE_CREATION_SUCCESS)

			EventLoggingUtils.logEvent(PersistOneInvoiceEvent(userIdFromSession(request), invoiceToPersist))

		} catch {
			case e: RuntimeException =>
				LOGGER.error(EXCEPTION_DURING_PERSISTING_INVOICE.format(invoiceToPersist), e)
				generalMessages += messageFactory.error(MSG_INVOICE_PERSISTING_FAILED)
		}
	}

	private def redirectUri(periodicalId: Long) = s"$PERIODICAL_LIST_URI/$periodicalId"
}
