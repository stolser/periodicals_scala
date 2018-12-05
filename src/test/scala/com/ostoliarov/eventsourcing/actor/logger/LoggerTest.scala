package com.ostoliarov.eventsourcing.actor.logger

import akka.testkit.TestProbe
import com.ostoliarov.eventsourcing.WordSpecAkkaTestKit
import com.ostoliarov.eventsourcing.actor.logger.LoggerManager.LogEventWithRetry
import com.ostoliarov.eventsourcing.actor.writer.ConsoleWriter.WriteEvent
import com.ostoliarov.eventsourcing.model.SignInEvent
import com.ostoliarov.webproject.model.entity.user.User

/**
	* Created by Oleg Stoliarov on 12/5/18.
	*/
class LoggerTest extends WordSpecAkkaTestKit {
	"Logger should use a writer actor which should receive WriteEvent with the same params" in {
		val testProbe = TestProbe()
		val writer = TestProbe()
		val logger = system.actorOf(Logger.props(writer = writer.ref))
		val requestId = 11
		val event = SignInEvent(User(id = 1), userIp = "1.2.3.4")

		logger.tell(
			LogEventWithRetry(requestId, event),
			testProbe.ref
		)

		val writerRequest = writer.expectMsgType[WriteEvent]
		assert(writerRequest.requestId === requestId)
		assert(writerRequest.event === event)

	}
}
