package com.stolser.javatraining.webproject.controller.request.processor.user

import java.util
import java.util.{Collections, List}

import com.stolser.javatraining.webproject.controller.ApplicationResources.{ONE_USER_INFO_VIEW_NAME, USER_INVOICES_PARAM_NAME, USER_SUBSCRIPTIONS_PARAM_NAME}
import com.stolser.javatraining.webproject.controller.request.processor.RequestProcessor
import com.stolser.javatraining.webproject.controller.utils.HttpUtils
import com.stolser.javatraining.webproject.model.entity.invoice.Invoice
import com.stolser.javatraining.webproject.model.entity.subscription.Subscription
import com.stolser.javatraining.webproject.service.impl.{InvoiceServiceImpl, PeriodicalServiceImpl, SubscriptionServiceImpl}
import com.stolser.javatraining.webproject.service.{InvoiceService, PeriodicalService, SubscriptionService}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

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
		val invoices: util.List[Invoice] = invoiceService.findAllByUserId(currentUserId)
		val subscriptions: util.List[Subscription] = subscriptionService.findAllByUserId(currentUserId)

		if (areThereInvoicesToDisplay(invoices)) {
			invoices.forEach(invoice => {
				val periodicalId: Long = invoice.getPeriodical.getId
				invoice.setPeriodical(periodicalService.findOneById(periodicalId))
			})

			sortInvoices(invoices)
			request.setAttribute(USER_INVOICES_PARAM_NAME, invoices)
		}

		if (areThereSubscriptionsToDisplay(subscriptions)) {
			sortSubscriptions(subscriptions)
			request.setAttribute(USER_SUBSCRIPTIONS_PARAM_NAME, subscriptions)
		}

		FORWARD + ONE_USER_INFO_VIEW_NAME
	}

	private def areThereInvoicesToDisplay(invoices: util.List[Invoice]) = !invoices.isEmpty

	private def areThereSubscriptionsToDisplay(subscriptions: util.List[Subscription]) = !subscriptions.isEmpty

	private def sortInvoices(invoices: util.List[Invoice]): Unit = {
		invoices.sort((first, second) => {
			if (first.getStatus == second.getStatus)
				if (Invoice.Status.NEW == first.getStatus) second.getCreationDate.compareTo(first.getCreationDate)
				else second.getPaymentDate.compareTo(first.getPaymentDate)
			else if (first.getStatus == Invoice.Status.NEW) -1
			else 1
		})
	}

	private def sortSubscriptions(subscriptions: util.List[Subscription]): Unit = {
		subscriptions.sort((first, second) => {
			if (first.getStatus == second.getStatus) first.getEndDate.compareTo(second.getEndDate)
			else if (first.getStatus == Subscription.Status.ACTIVE) -1
			else 1
		})
	}
}
