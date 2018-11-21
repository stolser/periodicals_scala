package com.ostoliarov.webproject.controller.request.processor.periodical

import com.ostoliarov.webproject.controller.ApplicationResources.{MSG_NO_PERIODICALS_TO_DELETE, MSG_PERIODICALS_DELETED_SUCCESS, PERIODICAL_LIST_URI}
import com.ostoliarov.webproject.controller.message.{FrontMessageFactory, FrontendMessage}
import com.ostoliarov.webproject.controller.request.processor.RequestProcessor
import com.ostoliarov.webproject.controller.utils.HttpUtils._
import com.ostoliarov.webproject.service.PeriodicalService
import com.ostoliarov.webproject.service.impl.mysql.PeriodicalServiceMysqlImpl
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import scala.collection.mutable

/**
	* Created by Oleg Stoliarov on 10/11/18.
	* Processes a POST request to delete all periodicals with status = "discarded".
	*/
object DeleteDiscardedPeriodicals extends RequestProcessor {
	private val periodicalService: PeriodicalService = PeriodicalServiceMysqlImpl
	private val messageFactory = FrontMessageFactory

	override def process(request: HttpServletRequest,
											 response: HttpServletResponse): String = {
		val generalMessages = mutable.ListBuffer[FrontendMessage]()

		persistPeriodicalsToDeleteAndRelatedData()
		addDeleteResultMessage(
			generalMessages,
			deletedPeriodicalsNumber = periodicalService.deleteAllDiscarded()
		)

		addGeneralMessagesToSession(request, generalMessages)

		REDIRECT + PERIODICAL_LIST_URI
	}

	private def addDeleteResultMessage(generalMessages: mutable.ListBuffer[FrontendMessage],
																		 deletedPeriodicalsNumber: Int): Unit = {
		val message =
			if (deletedPeriodicalsNumber > 0)
				messageFactory.success(MSG_PERIODICALS_DELETED_SUCCESS)
			else
				messageFactory.warning(MSG_NO_PERIODICALS_TO_DELETE)

		generalMessages += message
	}

	private def persistPeriodicalsToDeleteAndRelatedData(): Unit = {
		/*Here goes implementation of persisting somehow deleted data. It can be serialization into
		* files or saving into an archive database.*/
	}
}
