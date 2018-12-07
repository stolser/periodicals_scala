package com.ostoliarov.eventsourcing.logging.actor.logger

import akka.actor.Actor
import com.ostoliarov.eventsourcing.logging.actor.logger.LoggerManager.{LogEvent, LogEventWithRetry, StopAllLoggers}
import com.ostoliarov.eventsourcing.logging.actor.logger.impl.Logger.{LogEventFailure, LogEventSuccess, Stop}

/**
	* Created by Oleg Stoliarov on 12/7/18.
	*/
trait LoggerManagerBehavior {
	this: Actor with LoggerManagerState =>

	override def receive: Receive = {
		case LogEvent(event) =>
			val requestIdNumber = nextRequestIdNumber.getAndIncrement()
			for (logger <- loggers) {
				val requestId = s"${logger.path.name}_$requestIdNumber"
				logger ! LogEventWithRetry(requestId, event)
			}

		case LogEventFailure(requestId) =>
			failedRequestIds += requestId

		case LogEventSuccess(_) => Actor.emptyBehavior

		case StopAllLoggers => loggers.foreach(_ ! Stop)
	}
}
