package com.ostoliarov.webproject.controller.request.processor.periodical

import com.ostoliarov.webproject.controller.ApplicationResources.{ALL_PERIODICALS_ATTR_NAME, PERIODICAL_LIST_VIEW_NAME}
import com.ostoliarov.webproject.controller.request.processor.RequestProcessor
import com.ostoliarov.webproject.service.PeriodicalService
import com.ostoliarov.webproject.service.impl.mysql.PeriodicalServiceMysqlImpl
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import scala.collection.JavaConverters._

/**
  * Created by Oleg Stoliarov on 10/11/18.
  * Processes a GET request to a page displaying a list of periodicals.<br/>
  * The following logic is implemented by a corresponding JSP page:
  * - a user with role = 'subscriber' will see only those that have status = 'active'. <br/>
  * - a user with role = 'admin' will see all periodicals in the system.
  */
object DisplayAllPeriodicals extends RequestProcessor {
	private val periodicalService: PeriodicalService = PeriodicalServiceMysqlImpl

	override def process(request: HttpServletRequest,
											 response: HttpServletResponse): String = {
		request.setAttribute(ALL_PERIODICALS_ATTR_NAME, periodicalService.findAll.asJava)

		FORWARD + PERIODICAL_LIST_VIEW_NAME
	}
}
