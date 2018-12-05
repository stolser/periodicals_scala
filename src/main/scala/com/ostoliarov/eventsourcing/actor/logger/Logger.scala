package com.ostoliarov.eventsourcing.actor.logger

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.ostoliarov.eventsourcing.actor.logger.Logger.{LogEventFailure, LogEventSuccess}
import com.ostoliarov.eventsourcing.actor.logger.LoggerManager.LogEventWithRetry
import com.ostoliarov.eventsourcing.actor.writer.ConsoleWriter.WriteEvent
import com.ostoliarov.eventsourcing.model.Event

import scala.collection.mutable

/**
	* Created by Oleg Stoliarov on 12/5/18.
	*/
object Logger {
	def props(writer: ActorRef): Props = Props(new Logger(writer))

	final case class LogEventSuccess(requestId: Long)

	final case class LogEventFailure(requestId: Long)

	case object Stop

}

class Logger(writer: ActorRef) extends Actor with ActorLogging {
	private val requestRetries = mutable.Map[Long, Int]()
	private val requestId2Events = mutable.Map[Long, Event]()
	private val RetryNumberLimit = 5

	override def receive: Receive = {
		case LogEventWithRetry(requestId, event) =>
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

		case LogEventSuccess(requestId) =>
			requestRetries.remove(requestId)
			requestId2Events.remove(requestId)
			context.actorSelection("..") ! LogEventSuccess(requestId)
	}

	override def preStart(): Unit = log.info(s"Starting Logger '${self.path.name}' with writer = '$writer'...")

	override def postStop(): Unit = log.info(s"Stopping Logger '${self.path.name}'...")
}
