package com.stolser.javatraining.webproject.controller.request.processor.invoice

import java.time.Instant

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
import org.slf4j.LoggerFactory

import scala.collection.mutable

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
		val generalMessages = mutable.ListBuffer[FrontendMessage]()
		val periodicalId = java.lang.Long.parseLong(request.getParameter(PERIODICAL_ID_PARAM_NAME))
		val periodicalInDb = periodicalService.findOneById(periodicalId)

		periodicalInDb match {
			case Some(periodical) if isPeriodicalValid(periodical, request, generalMessages) =>
				tryToPersistNewInvoice(getNewInvoice(periodical, request), generalMessages)
			case None =>
				generalMessages += messageFactory.error(MSG_VALIDATION_PERIODICAL_IS_NULL)
		}

		HttpUtils.addGeneralMessagesToSession(request, generalMessages)

		REDIRECT + redirectUri(periodicalId)
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

	private def getNewInvoice(periodicalInDb: Periodical,
														request: HttpServletRequest) = {
		val subscriptionPeriod = request.getParameter(SUBSCRIPTION_PERIOD_PARAM_NAME).toInt

		val totalSum = subscriptionPeriod * periodicalInDb.oneMonthCost
		val userIdFromUri = HttpUtils.firstIdFromUri(request.getRequestURI)
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

	private def tryToPersistNewInvoice(invoiceToPersist: Invoice,
																		 generalMessages: mutable.ListBuffer[FrontendMessage]): Unit = {
		generalMessages += messageFactory.info(MSG_VALIDATION_PASSED_SUCCESS)
		try {
			invoiceService.createNew(invoiceToPersist)
			generalMessages += messageFactory.success(MSG_INVOICE_CREATION_SUCCESS)
		} catch {
			case e: RuntimeException =>
				LOGGER.error(String.format(EXCEPTION_DURING_PERSISTING_INVOICE, invoiceToPersist), e)
				generalMessages += messageFactory.error(MSG_INVOICE_PERSISTING_FAILED)
		}
	}

	private def redirectUri(periodicalId: Long) = s"$PERIODICAL_LIST_URI/$periodicalId"
}
