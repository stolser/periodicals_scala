package com.ostoliarov.eventsourcing.logging.actor.logger

import akka.actor.{Actor, ActorRef, Props}
import akka.testkit.{TestKit, TestProbe}
import com.ostoliarov.eventsourcing.WordSpecAkkaTestKit
import com.ostoliarov.eventsourcing.logging.actor.logger.LoggerManager.{LogEvent, LogEventWithRetry}
import com.ostoliarov.eventsourcing.logging.model.SignInEvent
import com.ostoliarov.eventsourcing.logging.{EventLoggingUtils, RequestId}

import scala.collection.mutable

/**
	* Created by Oleg Stoliarov on 12/8/18.
	* This test was moved into a separate class, since it checks the globally used 'initEventUUID',
	* which is changed after creating a new event, so other tests don't interfere with it.
	*/
class LoggerManagerRequestIdTest extends WordSpecAkkaTestKit {
	val loggerName = "logger-test"
	val signInEvent = SignInEvent(userId = 1, userIp = "1.2.3.4")
	val initRequestIdNumber: Int = EventLoggingUtils.initEventUUID

	override def afterAll: Unit = {
		TestKit.shutdownActorSystem(system)
	}

	def fixture = new {
		val probeA = TestProbe()
		val probeB = TestProbe()
		val loggerTestProbes = Set(probeA, probeB)
		val loggingFailures: mutable.Set[RequestId] = mutable.Set.empty
		val loggerManager: ActorRef = system.actorOf(
			props = Props(new Actor with LoggerManagerBehavior with LoggerManagerState {
				override val loggers: Set[ActorRef] = loggerTestProbes.map(_.ref)
				override val failedRequestIds: mutable.Set[RequestId] = loggingFailures
			})
		)
	}

	"LoggerManager" when {
		"receiving LogEvent(event)" should {
			val f = fixture
			f.loggerManager.tell(LogEvent(signInEvent), testActor)

			"generate a correct requestId and" should {
				val requestIdA = s"${f.probeA.ref.path.name}_$initRequestIdNumber"
				val requestIdB = s"${f.probeB.ref.path.name}_$initRequestIdNumber"
				val expectedRequestIds = Set(requestIdA, requestIdB)

				"send LogEventWithRetry with the requestId and event" in {
					val expectMsgA = f.probeA.expectMsgType[LogEventWithRetry]
					assert(expectMsgA.event === signInEvent)

					val expectMsgB = f.probeB.expectMsgType[LogEventWithRetry]
					assert(expectMsgB.event === signInEvent)

					val actualRequestIds = Set(expectMsgA.requestId, expectMsgB.requestId)

					assert(actualRequestIds === expectedRequestIds)
				}
			}
		}
	}
}
