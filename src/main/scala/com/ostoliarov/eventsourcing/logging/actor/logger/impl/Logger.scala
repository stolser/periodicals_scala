package com.ostoliarov.eventsourcing.logging.actor.logger.impl

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.ostoliarov.eventsourcing.logging._
import com.ostoliarov.eventsourcing.logging.actor.logger.{LoggerBehavior, LoggerState}
import com.ostoliarov.eventsourcing.logging.model.Event

import scala.collection.mutable

/**
	* Created by Oleg Stoliarov on 12/5/18.
	*/
private[actor] object Logger {
	def props(writer: ActorRef): Props = Props(new Logger(writer))

	final case class LogEventSuccess(requestId: RequestId)

	final case class LogEventFailure(requestId: RequestId)

	case object Stop

	case object WriterIsAlive

}

private[impl] class Logger(val writer: ActorRef) extends Actor
	with LoggerState
	with LoggerBehavior
	with ActorLogging {

	override val requestRetries: mutable.Map[RequestId, Retry] = mutable.Map.empty
	override val requestId2Events: mutable.Map[RequestId, Event] = mutable.Map.empty
	override val RetryNumberLimit = 5

	override def preStart(): Unit = log.info(s"Starting Logger '${self.path.name}' with writer = '$writer'...")

	override def postStop(): Unit = log.info(s"Stopping Logger '${self.path.name}'...")
}
