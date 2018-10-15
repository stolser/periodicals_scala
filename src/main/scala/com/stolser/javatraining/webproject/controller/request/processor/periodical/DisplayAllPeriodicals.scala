package com.stolser.javatraining.webproject.controller.request.processor.periodical

import com.stolser.javatraining.webproject.controller.ApplicationResources.{
	ALL_PERIODICALS_ATTR_NAME,
	PERIODICAL_LIST_VIEW_NAME
}
import com.stolser.javatraining.webproject.controller.request.processor.RequestProcessor
import com.stolser.javatraining.webproject.service.PeriodicalService
import com.stolser.javatraining.webproject.service.impl.PeriodicalServiceImpl
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

/**
  * Created by Oleg Stoliarov on 10/11/18.
  * Processes a GET request to a page displaying a list of periodicals.<br/>
  * The following logic is implemented by a corresponding JSP page:
  * - a user with role = 'subscriber' will see only those that have status = 'active'. <br/>
  * - a user with role = 'admin' will see all periodicals in the system.
  */
object DisplayAllPeriodicals extends RequestProcessor {
	private val periodicalService: PeriodicalService = PeriodicalServiceImpl

	override def process(request: HttpServletRequest, response: HttpServletResponse): String = {
		request.setAttribute(ALL_PERIODICALS_ATTR_NAME, periodicalService.findAll)
		FORWARD + PERIODICAL_LIST_VIEW_NAME
	}
}
