package com.stolser.javatraining.webproject.controller.request.processor.admin.panel

import java.time.Instant
import java.time.temporal.ChronoUnit

import com.stolser.javatraining.webproject.controller.ApplicationResources.{ADMIN_PANEL_VIEW_NAME, FINANCIAL_STATISTICS_ATTR_NAME, PERIODICAL_STATISTICS_ATTR_NAME}
import com.stolser.javatraining.webproject.controller.request.processor.RequestProcessor
import com.stolser.javatraining.webproject.service.impl.mysql.{InvoiceServiceMysqlImpl, PeriodicalServiceMysqlImpl}
import com.stolser.javatraining.webproject.service.{InvoiceService, PeriodicalService}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import scala.collection.JavaConverters._

/**
	* Created by Oleg Stoliarov on 10/10/18.
	* Processes a GET request to the Admin Panel page.
	*/
object DisplayAdminPanel extends RequestProcessor {
	private val periodicalService: PeriodicalService = PeriodicalServiceMysqlImpl
	private val invoiceService: InvoiceService = InvoiceServiceMysqlImpl

	override def process(request: HttpServletRequest,
											 response: HttpServletResponse): String = {
		addPeriodicalStatsIntoRequest(request)
		addFinStatsIntoRequest(request)

		FORWARD + ADMIN_PANEL_VIEW_NAME
	}

	private def addPeriodicalStatsIntoRequest(request: HttpServletRequest): Unit =
		request.setAttribute(
			PERIODICAL_STATISTICS_ATTR_NAME,
			periodicalService.quantitativeStatistics.asJava
		)

	private def addFinStatsIntoRequest(request: HttpServletRequest): Unit =
		request.setAttribute(
			FINANCIAL_STATISTICS_ATTR_NAME,
			invoiceService.finStatistics(
				since = Instant.now.minus(30, ChronoUnit.DAYS),
				until = Instant.now
			)
		)
}
