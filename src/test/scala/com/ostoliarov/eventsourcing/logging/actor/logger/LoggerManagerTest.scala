package com.ostoliarov.eventsourcing.logging.actor.logger

import java.util.concurrent.atomic.AtomicLong

import akka.actor.{Actor, ActorRef, Props}
import akka.testkit.{TestKit, TestProbe}
import com.ostoliarov.eventsourcing.logging.actor.logger.impl.Logger.{LogEventFailure, Stop}
import com.ostoliarov.eventsourcing.logging.actor.logger.LoggerManager.{LogEvent, LogEventWithRetry, StopAllLoggers}
import com.ostoliarov.eventsourcing.logging.model.SignInEvent
import com.ostoliarov.eventsourcing.WordSpecAkkaTestKit
import com.ostoliarov.eventsourcing.logging._

import scala.collection.mutable

/**
	* Created by Oleg Stoliarov on 12/7/18.
	*/

class LoggerManagerTest extends WordSpecAkkaTestKit {
	val loggerName = "logger-test"
	val signInEvent = SignInEvent(userId = 1, userIp = "1.2.3.4")
	val initRequestIdNumber = 10

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
				override val nextRequestIdNumber: AtomicLong = new AtomicLong(initRequestIdNumber)
			})
		)
	}

	"LoggerManager" when {
		"receiving LogEvent" should {
			"send LogEventWithRetry to every existing logger" in {
				val f = fixture

				f.loggerManager.tell(LogEvent(signInEvent), testActor)

				f.loggerTestProbes.foreach(_.expectMsgType[LogEventWithRetry])
			}
		}
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

	"LoggerManager" when {
		"receiving LogEventFailure" should {
			"store the received requestId" in {
				val failedRequestId = "logger_777"
				val f = fixture

				f.loggerManager.tell(LogEventFailure(failedRequestId), testActor)

				assert(f.loggingFailures.contains(failedRequestId))
			}
		}
	}

	"LoggerManager" when {
		"receiving StopAllLoggers" should {
			"send Stop to all existing loggers" in {
				val f = fixture

				f.loggerManager.tell(StopAllLoggers, testActor)

				f.loggerTestProbes.foreach(_.expectMsgType[Stop.type])
			}
		}
	}
}
