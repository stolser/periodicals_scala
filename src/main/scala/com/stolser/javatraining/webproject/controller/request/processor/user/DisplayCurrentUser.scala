package com.stolser.javatraining.webproject.controller.request.processor.user

import java.time.Instant

import com.stolser.javatraining.webproject.controller.ApplicationResources.{ONE_USER_INFO_VIEW_NAME, USER_INVOICES_PARAM_NAME, USER_SUBSCRIPTIONS_PARAM_NAME}
import com.stolser.javatraining.webproject.controller.request.processor.RequestProcessor
import com.stolser.javatraining.webproject.controller.utils.HttpUtils
import com.stolser.javatraining.webproject.model.entity.invoice.{Invoice, InvoiceStatus}
import com.stolser.javatraining.webproject.model.entity.subscription.{Subscription, SubscriptionStatus}
import com.stolser.javatraining.webproject.service.impl.{InvoiceServiceImpl, PeriodicalServiceImpl, SubscriptionServiceImpl}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
  * Created by Oleg Stoliarov on 10/11/18.
  * Processes a GET request to a current user personal account page.
  */
object DisplayCurrentUser extends RequestProcessor {
	private val invoiceService = InvoiceServiceImpl
	private val subscriptionService = SubscriptionServiceImpl
	private val periodicalService = PeriodicalServiceImpl

	override def process(request: HttpServletRequest, response: HttpServletResponse): String = {
		val currentUserId: Long = HttpUtils.getUserIdFromSession(request)
		val invoices: mutable.Buffer[Invoice] = invoiceService.findAllByUserId(currentUserId).asScala
		val subscriptions: mutable.Buffer[Subscription] = subscriptionService.findAllByUserId(currentUserId).asScala

		if (invoices nonEmpty) {
			invoices.foreach(invoice =>
				invoice.periodical = periodicalService.findOneById(id = invoice.periodical.id)
			)

			request.setAttribute(
				USER_INVOICES_PARAM_NAME,
				sortInvoices(invoices).asJava
			)
		}

		if (subscriptions nonEmpty)
			request.setAttribute(
				USER_SUBSCRIPTIONS_PARAM_NAME,
				sortSubscriptions(subscriptions).asJava
			)


		FORWARD + ONE_USER_INFO_VIEW_NAME
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
