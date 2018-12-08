package com.ostoliarov.eventsourcing.logging.actor.logger

import akka.actor.{Actor, ActorRef, Props}
import akka.testkit.{TestKit, TestProbe}
import com.ostoliarov.eventsourcing.WordSpecAkkaTestKit
import com.ostoliarov.eventsourcing.logging._
import com.ostoliarov.eventsourcing.logging.actor.logger.LoggerManager.{LogEvent, LogEventWithRetry, StopAllLoggers}
import com.ostoliarov.eventsourcing.logging.actor.logger.impl.Logger.Stop
import com.ostoliarov.eventsourcing.logging.model.SignInEvent

import scala.collection.mutable

/**
	* Created by Oleg Stoliarov on 12/7/18.
	*/

class LoggerManagerTest extends WordSpecAkkaTestKit {
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
		"receiving LogEvent" should {
			"send LogEventWithRetry to every existing logger" in {
				val f = fixture

				f.loggerManager.tell(LogEvent(signInEvent), testActor)

				f.loggerTestProbes.foreach(_.expectMsgType[LogEventWithRetry])
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
