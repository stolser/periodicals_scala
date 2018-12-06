package com.ostoliarov.eventsourcing.actor.writer

import akka.testkit.TestProbe
import com.ostoliarov.eventsourcing.WordSpecAkkaTestKit
import com.ostoliarov.eventsourcing.actor.logger.Logger.LogEventSuccess
import com.ostoliarov.eventsourcing.actor.writer.ConsoleWriter.WriteEvent
import com.ostoliarov.eventsourcing.model.SignInEvent

/**
	* Created by Oleg Stoliarov on 12/5/18.
	*/
class ConsoleWriterTest extends WordSpecAkkaTestKit {
	"ConsoleWriter should send LogEventSuccess if an event was logged successfully" in {
		val testProbe = TestProbe()
		val consoleWriter = system.actorOf(ConsoleWriter.props(withFailures = false))
		consoleWriter.tell(
			WriteEvent(requestId = 11, event = SignInEvent(userId = 1, userIp = "1.2.3.4")),
			testProbe.ref
		)

		val response = testProbe.expectMsgType[LogEventSuccess]
		assert(response.requestId === 11)

	}
}
