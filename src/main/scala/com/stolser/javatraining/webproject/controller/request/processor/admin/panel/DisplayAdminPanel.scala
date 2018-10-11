package com.stolser.javatraining.webproject.controller.request.processor.admin.panel

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util
import java.util.List

import com.stolser.javatraining.webproject.controller.ApplicationResources.{ADMIN_PANEL_VIEW_NAME, FINANCIAL_STATISTICS_ATTR_NAME, PERIODICAL_STATISTICS_ATTR_NAME}
import com.stolser.javatraining.webproject.controller.request.processor.RequestProcessor
import com.stolser.javatraining.webproject.model.entity.statistics.{FinancialStatistics, PeriodicalNumberByCategory}
import com.stolser.javatraining.webproject.service.impl.{InvoiceServiceImpl, PeriodicalServiceImpl}
import com.stolser.javatraining.webproject.service.{InvoiceService, PeriodicalService}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

/**
  * Created by Oleg Stoliarov on 10/10/18.
  * Processes a GET request to the Admin Panel page.
  */
object DisplayAdminPanel extends RequestProcessor {
	private val periodicalService: PeriodicalService = PeriodicalServiceImpl.getInstance
	private val invoiceService: InvoiceService = InvoiceServiceImpl.getInstance

	override def process(request: HttpServletRequest, response: HttpServletResponse): String = {
		addPeriodicalStatsIntoRequest(request)
		addFinStatsIntoRequest(request)

		FORWARD + ADMIN_PANEL_VIEW_NAME
	}

	private def addPeriodicalStatsIntoRequest(request: HttpServletRequest): Unit = {
		val periodicalStatistics: util.List[PeriodicalNumberByCategory] = periodicalService.getQuantitativeStatistics
		request.setAttribute(PERIODICAL_STATISTICS_ATTR_NAME, periodicalStatistics)
	}

	private def addFinStatsIntoRequest(request: HttpServletRequest): Unit = {
		val until: Instant = Instant.now
		val since: Instant = until.minus(30, ChronoUnit.DAYS)
		val finStatistics: FinancialStatistics = invoiceService.getFinStatistics(since, until)
		request.setAttribute(FINANCIAL_STATISTICS_ATTR_NAME, finStatistics)
	}
}
