package com.ostoliarov.eventsourcing.logging.actor.logger

import akka.actor.{Actor, ActorLogging}
import com.ostoliarov.eventsourcing.logging.actor.logger.LoggerManager.LogEventWithRetry
import com.ostoliarov.eventsourcing.logging.actor.logger.impl.Logger.{LogEventFailure, LogEventSuccess, Stop, WriterIsAlive}
import com.ostoliarov.eventsourcing.logging.actor.writer.ConsoleWriter.WriteEvent

/**
	* Created by Oleg Stoliarov on 12/7/18.
	*/
trait LoggerBehavior {
	this: Actor with ActorLogging with LoggerState =>

	override def receive: Receive = {
		case LogEventWithRetry(requestId, event) =>
			println(s"== Logger state: requestRetries=$requestRetries")

			requestId2Events += (requestId -> event)
			val increasedRetryNumber = requestRetries.getOrElse(requestId, 0) + 1
			requestRetries += (requestId -> increasedRetryNumber)

			if (requestRetries(requestId) <= RetryNumberLimit) {
				println(s"requestId=$requestId... ${requestRetries(requestId)}")
				writer ! WriteEvent(requestId, event)
			} else {
				requestRetries.remove(requestId)
				requestId2Events.remove(requestId)
				log.error(s"[requestId=$requestId]: Logging event (${event.eventMessage}) has failed.")
				context.actorSelection("..") ! LogEventFailure(requestId)
			}

		case LogEventFailure(requestId) =>
			self ! LogEventWithRetry(requestId, requestId2Events(requestId))

		case WriterIsAlive =>
			requestRetries.foreach({ case (requestId, _) => self ! LogEventFailure(requestId) })

		case LogEventSuccess(requestId) =>
			requestRetries.remove(requestId)
			requestId2Events.remove(requestId)
			context.actorSelection("..") ! LogEventSuccess(requestId)

		case Stop => context.stop(self)
	}
}
