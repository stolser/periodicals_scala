package com.ostoliarov.eventsourcing.logging.actor.writer

import akka.testkit.{EventFilter, TestKit}
import com.ostoliarov.eventsourcing.WordSpecAkkaTestKit
import com.ostoliarov.eventsourcing.logging.actor.logger.impl.Logger.{LogEventFailure, LogEventSuccess}
import com.ostoliarov.eventsourcing.logging.actor.writer.ConsoleWriter.WriteEvent
import com.ostoliarov.eventsourcing.logging.model.SignInEvent

/**
	* Created by Oleg Stoliarov on 12/5/18.
	*/
class ConsoleWriterTest extends WordSpecAkkaTestKit {
	val signInEvent = SignInEvent(userId = 1, userIp = "1.2.3.4")
	val correctRequestId = "Logger_11"
	val incorrectRequestId = "Logger_0"

	override def afterAll: Unit = {
		TestKit.shutdownActorSystem(system)
	}

	def fixture = new {
		val consoleWriter = system.actorOf(ConsoleWriter.props(withFailures = false))
	}

	"ConsoleWriter" when {
		"an event was logged successfully" should {
			"send LogEventSuccess to the sender" in {
				fixture.consoleWriter.tell(
					WriteEvent(requestId = correctRequestId, event = signInEvent),
					testActor
				)

				val response = expectMsgType[LogEventSuccess]
				assert(response.requestId === correctRequestId)
			}
		}
	}

	"ConsoleWriter" when {
		"there is an error" should {
			"send LogEventFailure to the sender" in {
				fixture.consoleWriter.tell(
					WriteEvent(requestId = incorrectRequestId, event = signInEvent),
					testActor
				)

				val response = expectMsgType[LogEventFailure]
				assert(response.requestId === incorrectRequestId)
			}
		}
	}

	"ConsoleWriter" when {
		"there is no errors" should {
			"log the event" in {
				EventFilter.info(start = s"[requestId=$correctRequestId]", occurrences = 1) intercept {
					fixture.consoleWriter.tell(
						WriteEvent(requestId = correctRequestId, event = signInEvent),
						testActor
					)
				}
			}
		}
	}
}
