package com.ostoliarov.webproject.controller.request.processor.periodical

import java.util.{Map => JavaMap}

import com.ostoliarov.webproject.controller.ApplicationResources._
import com.ostoliarov.webproject.controller.message.FrontendMessage
import com.ostoliarov.webproject.controller.request.processor.DispatchType._
import com.ostoliarov.webproject.controller.request.processor.{AbstractViewName, RequestProcessor, ResourceRequest}
import com.ostoliarov.webproject.model.entity.periodical.{Periodical, PeriodicalCategory, PeriodicalOperationType, PeriodicalStatus}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

/**
	* Created by Oleg Stoliarov on 10/11/18.
	* Processes a GET request to a page where admin can create a new periodical.
	*/
object DisplayNewPeriodicalPage extends RequestProcessor {
	override def process(request: HttpServletRequest,
											 response: HttpServletResponse): ResourceRequest = {
		request.getSession.removeAttribute(PERIODICAL_ATTR_NAME)
		setRequestAttributes(request)

		ResourceRequest(FORWARD, AbstractViewName(CREATE_EDIT_PERIODICAL_VIEW_NAME))
	}

	private def setRequestAttributes(request: HttpServletRequest): Unit = {
		import scala.collection.JavaConverters.asJavaIterableConverter

		val periodicalToForward: Periodical =
			getPeriodicalFromSession(request) match {
				case Some(periodical) => periodical
				case None => Periodical()
			}

		request.setAttribute(MESSAGES_ATTR_NAME, getMessagesFromSession(request))
		request.setAttribute(PERIODICAL_ATTR_NAME, periodicalToForward)
		request.setAttribute(PERIODICAL_STATUSES_ATTR_NAME, PeriodicalStatus.values.asJava)
		request.setAttribute(PERIODICAL_CATEGORIES_ATTR_NAME, PeriodicalCategory.values.asJava)
		request.setAttribute(
			PERIODICAL_OPERATION_TYPE_PARAM_ATTR_NAME,
			PeriodicalOperationType.CREATE.toString.toLowerCase
		)
	}

	private def getPeriodicalFromSession(request: HttpServletRequest) =
		Option(request.getSession.getAttribute(PERIODICAL_ATTR_NAME).asInstanceOf[Periodical])

	@SuppressWarnings(Array("unchecked"))
	private def getMessagesFromSession(request: HttpServletRequest): JavaMap[String, FrontendMessage] =
		request.getSession.getAttribute(MESSAGES_ATTR_NAME).asInstanceOf[JavaMap[String, FrontendMessage]]
}
