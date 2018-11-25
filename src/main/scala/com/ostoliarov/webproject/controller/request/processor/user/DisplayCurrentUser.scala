package com.ostoliarov.webproject.controller.request.processor.user

import java.time.Instant

import com.ostoliarov.webproject.controller.ApplicationResources.{ONE_USER_INFO_VIEW_NAME, USER_INVOICES_PARAM_NAME, USER_SUBSCRIPTIONS_PARAM_NAME}
import com.ostoliarov.webproject.controller.request.processor.DispatchType.FORWARD
import com.ostoliarov.webproject.controller.request.processor.{AbstractViewName, RequestProcessor, ResourceRequest}
import com.ostoliarov.webproject.controller.utils.HttpUtils._
import com.ostoliarov.webproject.model.entity.invoice.{Invoice, InvoiceStatus}
import com.ostoliarov.webproject.model.entity.subscription.{Subscription, SubscriptionStatus}
import com.ostoliarov.webproject.service.impl.mysql.{InvoiceServiceMysqlImpl, PeriodicalServiceMysqlImpl, SubscriptionServiceMysqlImpl}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
	* Created by Oleg Stoliarov on 10/11/18.
	* Processes a GET request to a current user personal account page.
	*/
object DisplayCurrentUser extends RequestProcessor {
	private val invoiceService = InvoiceServiceMysqlImpl
	private val subscriptionService = SubscriptionServiceMysqlImpl
	private val periodicalService = PeriodicalServiceMysqlImpl

	override def process(request: HttpServletRequest,
											 response: HttpServletResponse): ResourceRequest = {
		val currentUserId = userIdFromSession(request)
		val invoices = mutable.Buffer(invoiceService.findAllByUserId(currentUserId): _*)
		val subscriptions = mutable.Buffer(subscriptionService.findAllByUserId(currentUserId): _*)

		if (invoices.nonEmpty) {
			invoices.foreach(invoice =>
				periodicalService.findOneById(invoice.periodical.id) match {
					case Some(periodical) => invoice.periodical = periodical
					case None => throw new NoSuchElementException(s"A periodical with id = ${invoice.periodical.id} " +
						s"is missing for an existing invoice = $invoice")
				}
			)

			request.setAttribute(
				USER_INVOICES_PARAM_NAME,
				sortInvoices(invoices).asJava
			)
		}

		if (subscriptions.nonEmpty)
			request.setAttribute(
				USER_SUBSCRIPTIONS_PARAM_NAME,
				sortSubscriptions(subscriptions).asJava
			)

		ResourceRequest(FORWARD, AbstractViewName(ONE_USER_INFO_VIEW_NAME))
	}

	private def sortInvoices(invoices: mutable.Buffer[Invoice]) =
		invoices.sortWith((first, second) => {
			if (first.status == second.status)
				if (InvoiceStatus.NEW == first.status)
					compareInvoiceDates(first.creationDate, second.creationDate) > 0
				else
					compareInvoiceDates(first.paymentDate, second.paymentDate) > 0
			else if (first.status == InvoiceStatus.NEW)
				true
			else
				false
		})

	private def compareInvoiceDates(first: Option[Instant],
																	second: Option[Instant]) =
		(first, second) match {
			case (Some(firstDate), Some(secondDate)) => firstDate.compareTo(secondDate)
			case _ => throw new IllegalStateException(s"Something wrong with the date fields " +
				s"of the invoices (first: $first; second: $second)")
		}

	private def sortSubscriptions(subscriptions: mutable.Buffer[Subscription]) =
		subscriptions.sortWith((first, second) => {
			if (first.status == second.status)
				compareSubscriptionDates(first.endDate, second.endDate) < 0
			else if (first.status == SubscriptionStatus.ACTIVE)
				true
			else
				false
		})

	private def compareSubscriptionDates(first: Option[Instant],
																			 second: Option[Instant]) =
		(first, second) match {
			case (Some(firstDate), Some(secondDate)) => firstDate.compareTo(secondDate)
			case _ => throw new IllegalStateException(s"Something wrong with the end date " +
				s"of the subscriptions (first: $first; second: $second)")
		}
}
