package com.stolser.javatraining.webproject.controller.request.processor.periodical

import java.util

import com.stolser.javatraining.webproject.controller.ApplicationResources.{MSG_NO_PERIODICALS_TO_DELETE, MSG_PERIODICALS_DELETED_SUCCESS, PERIODICAL_LIST_URI}
import com.stolser.javatraining.webproject.controller.message.{FrontMessageFactory, FrontendMessage}
import com.stolser.javatraining.webproject.controller.request.processor.RequestProcessor
import com.stolser.javatraining.webproject.controller.utils.HttpUtils
import com.stolser.javatraining.webproject.controller.utils.HttpUtils._
import com.stolser.javatraining.webproject.service.PeriodicalService
import com.stolser.javatraining.webproject.service.impl.PeriodicalServiceImpl
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import scala.collection.mutable
import scala.collection.JavaConverters._

/**
  * Created by Oleg Stoliarov on 10/11/18.
  * Processes a POST request to delete all periodicals with status = "discarded".
  */
object DeleteDiscardedPeriodicals extends RequestProcessor {
	private val periodicalService: PeriodicalService = PeriodicalServiceImpl
	private val messageFactory = FrontMessageFactory

	override def process(request: HttpServletRequest, response: HttpServletResponse): String = {
		val generalMessages = mutable.ListBuffer[FrontendMessage]()

		persistPeriodicalsToDeleteAndRelatedData()
		addDeleteResultMessage(generalMessages,
			deletedPeriodicalsNumber = periodicalService.deleteAllDiscarded())

		addGeneralMessagesToSession(request, generalMessages)

		REDIRECT + PERIODICAL_LIST_URI
	}

	private def addDeleteResultMessage(generalMessages: mutable.ListBuffer[FrontendMessage],
									   deletedPeriodicalsNumber: Int): Unit = {
		val message =
			if (deletedPeriodicalsNumber > 0)
				messageFactory.getSuccess(MSG_PERIODICALS_DELETED_SUCCESS)
			else
				messageFactory.getWarning(MSG_NO_PERIODICALS_TO_DELETE)

		generalMessages += message
	}

	private def persistPeriodicalsToDeleteAndRelatedData(): Unit = {
		/*Here goes implementation of persisting somehow deleted data. It can be serialization into
		* files or saving into an archive database.*/
	}
}
