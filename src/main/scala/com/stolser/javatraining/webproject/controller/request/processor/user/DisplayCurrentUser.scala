package com.stolser.javatraining.webproject.controller.request.processor.user

import java.time.Instant
import java.util

import com.stolser.javatraining.webproject.controller.ApplicationResources.{ONE_USER_INFO_VIEW_NAME, USER_INVOICES_PARAM_NAME, USER_SUBSCRIPTIONS_PARAM_NAME}
import com.stolser.javatraining.webproject.controller.request.processor.RequestProcessor
import com.stolser.javatraining.webproject.controller.utils.HttpUtils
import com.stolser.javatraining.webproject.model.entity.invoice.{Invoice, InvoiceStatus}
import com.stolser.javatraining.webproject.model.entity.subscription.{Subscription, SubscriptionStatus}
import com.stolser.javatraining.webproject.service.impl.{InvoiceServiceImpl, PeriodicalServiceImpl, SubscriptionServiceImpl}
import com.stolser.javatraining.webproject.service.{InvoiceService, PeriodicalService, SubscriptionService}
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

		if (invoices.nonEmpty) {
			invoices.foreach(invoice => {
				val periodicalId: Long = invoice.periodical.getId
				invoice.periodical = periodicalService.findOneById(periodicalId)
			})

			val sorted = sortInvoices(invoices)
			request.setAttribute(USER_INVOICES_PARAM_NAME, sorted.asJava)
		}

		if (subscriptions.nonEmpty) {
			val sorted = sortSubscriptions(subscriptions)
			request.setAttribute(USER_SUBSCRIPTIONS_PARAM_NAME, sorted.asJava)
		}

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

	private def compareInvoiceDates(first: Option[Instant], second: Option[Instant]) =
		(first, second) match {
			case (Some(firstDate), Some(secondDate)) => firstDate.compareTo(secondDate)
//			case (Some(_), None) => -1
//			case (None, Some(_)) => 1
//			case (None, None) => 0
		}

	private def sortSubscriptions(subscriptions: mutable.Buffer[Subscription]) =
		subscriptions.sortWith((first, second) => {
			if (first.getStatus == second.getStatus)
				compareSubscriptionDates(first.getEndDate, second.getEndDate) < 0
			else if (first.getStatus == SubscriptionStatus.ACTIVE)
				true
			else
				false
		})

	private def compareSubscriptionDates(first: Option[Instant], second: Option[Instant]) =
		(first, second) match {
			case (Some(firstDate), Some(secondDate)) => firstDate.compareTo(secondDate)
//			case (Some(_), None) => -1
//			case (None, Some(_)) => 1
//			case (None, None) => 0
		}
}
