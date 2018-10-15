package com.stolser.javatraining.webproject.controller.request.processor.periodical

import java.util

import com.stolser.javatraining.webproject.controller.ApplicationResources.{
	MSG_NO_PERIODICALS_TO_DELETE,
	MSG_PERIODICALS_DELETED_SUCCESS,
	PERIODICAL_LIST_URI
}
import com.stolser.javatraining.webproject.controller.message.{FrontMessageFactory, FrontendMessage}
import com.stolser.javatraining.webproject.controller.request.processor.RequestProcessor
import com.stolser.javatraining.webproject.controller.utils.HttpUtils
import com.stolser.javatraining.webproject.service.PeriodicalService
import com.stolser.javatraining.webproject.service.impl.PeriodicalServiceImpl
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

/**
  * Created by Oleg Stoliarov on 10/11/18.
  * Processes a POST request to delete all periodicals with status = "discarded".
  */
object DeleteDiscardedPeriodicals extends RequestProcessor {
	private val periodicalService: PeriodicalService = PeriodicalServiceImpl
	private val messageFactory = FrontMessageFactory

	override def process(request: HttpServletRequest, response: HttpServletResponse): String = {
		val generalMessages: util.List[FrontendMessage] = new util.ArrayList[FrontendMessage]

		persistPeriodicalsToDeleteAndRelatedData()
		val deletedPeriodicalsNumber: Int = periodicalService.deleteAllDiscarded()
		addDeleteResultMessage(generalMessages, deletedPeriodicalsNumber)

		HttpUtils.addGeneralMessagesToSession(request, generalMessages)

		REDIRECT + PERIODICAL_LIST_URI
	}

	private def addDeleteResultMessage(generalMessages: util.List[FrontendMessage], deletedPeriodicalsNumber: Int): Unit = {
		val message = if (deletedPeriodicalsNumber > 0) messageFactory.getSuccess(MSG_PERIODICALS_DELETED_SUCCESS)
		else messageFactory.getWarning(MSG_NO_PERIODICALS_TO_DELETE)
		generalMessages.add(message)
	}

	private def persistPeriodicalsToDeleteAndRelatedData(): Unit = {
		/*Here goes implementation of persisting somehow deleted data. It can be serialization into
		* files or saving into an archive database.*/
	}
}
