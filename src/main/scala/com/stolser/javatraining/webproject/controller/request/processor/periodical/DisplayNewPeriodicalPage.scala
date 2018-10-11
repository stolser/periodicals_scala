package com.stolser.javatraining.webproject.controller.request.processor.periodical

import java.util
import java.util.Map
import java.util.Objects.nonNull

import com.stolser.javatraining.webproject.controller.ApplicationResources._
import com.stolser.javatraining.webproject.controller.message.FrontendMessage
import com.stolser.javatraining.webproject.controller.request.processor.RequestProcessor
import com.stolser.javatraining.webproject.model.entity.periodical.{Periodical, PeriodicalCategory}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

/**
  * Created by Oleg Stoliarov on 10/11/18.
  * Processes a GET request to a page where admin can create a new periodical.
  */
object DisplayNewPeriodicalPage extends RequestProcessor {
	override def process(request: HttpServletRequest, response: HttpServletResponse): String = {
		request.getSession.removeAttribute(PERIODICAL_ATTR_NAME)
		setRequestAttributes(request)

		FORWARD + CREATE_EDIT_PERIODICAL_VIEW_NAME
	}

	private def setRequestAttributes(request: HttpServletRequest): Unit = {
		val periodicalFromSession: Periodical = getPeriodicalFromSession(request)
		val periodicalToForward: Periodical =
			if (nonNull(periodicalFromSession)) periodicalFromSession
			else new Periodical

		request.setAttribute(MESSAGES_ATTR_NAME, getMessagesFromSession(request))
		request.setAttribute(PERIODICAL_ATTR_NAME, periodicalToForward)
		request.setAttribute(PERIODICAL_STATUSES_ATTR_NAME, Periodical.Status.values)
		request.setAttribute(PERIODICAL_CATEGORIES_ATTR_NAME, PeriodicalCategory.values)
		request.setAttribute(PERIODICAL_OPERATION_TYPE_PARAM_ATTR_NAME, Periodical.OperationType.CREATE.name.toLowerCase)
	}

	private def getPeriodicalFromSession(request: HttpServletRequest) =
		request.getSession.getAttribute(PERIODICAL_ATTR_NAME).asInstanceOf[Periodical]

	@SuppressWarnings(Array("unchecked"))
	private def getMessagesFromSession(request: HttpServletRequest): util.Map[String, FrontendMessage] =
		request.getSession.getAttribute(MESSAGES_ATTR_NAME).asInstanceOf[util.Map[String, FrontendMessage]]
}