package com.stolser.javatraining.webproject.controller.request.processor.periodical

import java.util.NoSuchElementException
import java.util.Objects.isNull

import com.stolser.javatraining.webproject.controller.ApplicationResources._
import com.stolser.javatraining.webproject.controller.request.processor.RequestProcessor
import com.stolser.javatraining.webproject.controller.utils.HttpUtils
import com.stolser.javatraining.webproject.model.entity.periodical.{Periodical, PeriodicalCategory}
import com.stolser.javatraining.webproject.service.PeriodicalService
import com.stolser.javatraining.webproject.service.impl.PeriodicalServiceImpl
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

/**
  * Created by Oleg Stoliarov on 10/11/18.
  * Processes a GET request to a page where admin can update information of one periodical.
  */
object DisplayUpdatePeriodicalPage extends RequestProcessor {
	private val periodicalService: PeriodicalService = PeriodicalServiceImpl.getInstance

	override def process(request: HttpServletRequest, response: HttpServletResponse): String = {
		val periodicalId = HttpUtils.getFirstIdFromUri(request.getRequestURI)
		val periodical = periodicalService.findOneById(periodicalId)

		if (isNull(periodical))
			throw new NoSuchElementException(s"There is no periodical with id $periodicalId in the db.")

		setRequestAttributes(request, periodical)

		FORWARD + CREATE_EDIT_PERIODICAL_VIEW_NAME
	}

	private def setRequestAttributes(request: HttpServletRequest, periodical: Periodical): Unit = {
		request.setAttribute(PERIODICAL_ATTR_NAME, periodical)
		request.setAttribute(PERIODICAL_OPERATION_TYPE_PARAM_ATTR_NAME, Periodical.OperationType.UPDATE.name.toLowerCase)
		request.setAttribute(PERIODICAL_STATUSES_ATTR_NAME, Periodical.Status.values)
		request.setAttribute(PERIODICAL_CATEGORIES_ATTR_NAME, PeriodicalCategory.values)
	}
}
