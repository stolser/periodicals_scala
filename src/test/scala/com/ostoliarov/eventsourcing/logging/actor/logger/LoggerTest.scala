package com.ostoliarov.eventsourcing.logging.actor.logger

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.testkit.{EventFilter, TestKit, TestProbe}
import com.ostoliarov.eventsourcing.WordSpecAkkaTestKit
import com.ostoliarov.eventsourcing.logging._
import com.ostoliarov.eventsourcing.logging.actor.logger.LoggerManager.LogEventWithRetry
import com.ostoliarov.eventsourcing.logging.actor.logger.impl.Logger.LogEventFailure
import com.ostoliarov.eventsourcing.logging.actor.writer.ConsoleWriter.WriteEvent
import com.ostoliarov.eventsourcing.logging.model.{Event, SignInEvent}

import scala.collection.mutable

/**
	* Created by Oleg Stoliarov on 12/5/18.
	*/
class LoggerTest extends WordSpecAkkaTestKit {
	val requestId = "Logger_11"
	val signInEvent = SignInEvent(userId = 1, userIp = "1.2.3.4")

	override def afterAll: Unit = {
		TestKit.shutdownActorSystem(system)
	}

	def fixture = new {
		fixture =>

		val writerTestProbe = TestProbe()
		val requestRetries: mutable.Map[RequestId, Int] = mutable.Map.empty
		val requestId2Events: mutable.Map[RequestId, Event] = mutable.Map.empty
		val logger = system.actorOf(Props(new Actor with ActorLogging with LoggerState with LoggerBehavior {
			override val writer: ActorRef = writerTestProbe.ref
			override val requestRetries: mutable.Map[RequestId, Retry] = fixture.requestRetries
			override val requestId2Events: mutable.Map[RequestId, Event] = fixture.requestId2Events
			override val RetryNumberLimit: Retry = 2
		}))

	}

	"Logger" when {
		"receiving LogEventWithRetry" should {
			val f = fixture

			f.logger.tell(LogEventWithRetry(requestId, signInEvent), testActor)

			"send WriteEvent to the writer with the same requestId and event" in {
				val writerExpectMsg = f.writerTestProbe.expectMsgType[WriteEvent]
				assert(writerExpectMsg.requestId === requestId)
				assert(writerExpectMsg.event === signInEvent)
			}

			"update requestId2Events and requestRetries mapping accordingly" in {
				assert(f.requestRetries.contains(requestId)
					&& f.requestRetries(requestId) === 1)

				assert(f.requestId2Events.contains(requestId)
					&& f.requestId2Events(requestId) === signInEvent)
			}
		}
	}

	"Logger" when {
		"receiving LogEventWithRetry twice for the same requestId" should {
			val f = fixture

			f.logger.tell(LogEventWithRetry(requestId, signInEvent), testActor)
			f.logger.tell(LogEventFailure(requestId), testActor)

			"increase retry number for this request by 1" in {
				assert(f.requestRetries.contains(requestId)
					&& f.requestRetries(requestId) === 2)
			}

			"contain only 1 entry in requestId2Events mapping with correct requestId and event" in {
				assert(f.requestId2Events.size === 1)
				assert(f.requestId2Events.contains(requestId)
					&& f.requestId2Events(requestId) === signInEvent)
			}
		}
	}

	"Logger" when {
		"receiving LogEventWithRetry times exceeding RetryNumberLimit" should {
			val f = fixture

			f.logger.tell(LogEventWithRetry(requestId, signInEvent), testActor)
			f.logger.tell(LogEventFailure(requestId), testActor)
			f.logger.tell(LogEventFailure(requestId), testActor)

			"remove requestId from requestRetries and requestId2Events mappings" in {
				assert(!f.requestRetries.contains(requestId))
				assert(!f.requestId2Events.contains(requestId))
			}
		}
	}

	"Logger" when {
		"receiving LogEventWithRetry times exceeding RetryNumberLimit" should {
			val f = fixture

			f.logger.tell(LogEventWithRetry(requestId, signInEvent), testActor)
			f.logger.tell(LogEventFailure(requestId), testActor)

			"log an error" in {
				EventFilter.error(start = s"[requestId=$requestId]", occurrences = 1) intercept {
					f.logger.tell(LogEventFailure(requestId), testActor)
				}
			}
		}
	}

}
