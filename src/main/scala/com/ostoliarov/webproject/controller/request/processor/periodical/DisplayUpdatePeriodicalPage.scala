package com.ostoliarov.webproject.controller.request.processor.periodical

import java.util.NoSuchElementException

import com.ostoliarov.webproject.controller.ApplicationResources._
import com.ostoliarov.webproject.controller.request.processor.{AbstractViewName, RequestProcessor, ResourceRequest}
import com.ostoliarov.webproject.controller.utils.HttpUtils
import com.ostoliarov.webproject.controller.request.processor.DispatchType.FORWARD
import com.ostoliarov.webproject.model.entity.periodical.{Periodical, PeriodicalCategory, PeriodicalOperationType, PeriodicalStatus}
import com.ostoliarov.webproject.service.PeriodicalService
import com.ostoliarov.webproject.service.impl.mysql.PeriodicalServiceMysqlImpl
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

/**
	* Created by Oleg Stoliarov on 10/11/18.
	* Processes a GET request to a page where admin can update information of one periodical.
	*/
object DisplayUpdatePeriodicalPage extends RequestProcessor {
	private val periodicalService: PeriodicalService = PeriodicalServiceMysqlImpl

	override def process(request: HttpServletRequest,
											 response: HttpServletResponse): ResourceRequest = {
		val periodicalId = HttpUtils.firstIdFromUri(request.getRequestURI)
		val periodicalInDb = periodicalService.findOneById(periodicalId)

		periodicalInDb match {
			case None => throw new NoSuchElementException(s"There is no periodical with id $periodicalId in the db.")
			case Some(periodical) =>
				setRequestAttributes(request, periodical)

				ResourceRequest(FORWARD, AbstractViewName(CREATE_EDIT_PERIODICAL_VIEW_NAME))
		}
	}

	private def setRequestAttributes(request: HttpServletRequest,
																	 periodical: Periodical): Unit = {
		import scala.collection.JavaConverters.asJavaIterableConverter

		request.setAttribute(PERIODICAL_ATTR_NAME, periodical)
		request.setAttribute(PERIODICAL_OPERATION_TYPE_PARAM_ATTR_NAME, PeriodicalOperationType.UPDATE.toString.toLowerCase)
		request.setAttribute(PERIODICAL_STATUSES_ATTR_NAME, PeriodicalStatus.values.asJava)
		request.setAttribute(PERIODICAL_CATEGORIES_ATTR_NAME, PeriodicalCategory.values.asJava)
	}
}
