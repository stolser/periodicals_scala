package com.ostoliarov.eventsourcing.logging.actor.logger

import java.util.concurrent.atomic.AtomicLong

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.ostoliarov.eventsourcing.EventSourcingSupervisor.EventLogSupervisorName
import com.ostoliarov.eventsourcing.logging._
import com.ostoliarov.eventsourcing.logging.actor.logger.impl.Logger
import com.ostoliarov.eventsourcing.logging.actor.writer.ConsoleWriter
import com.ostoliarov.eventsourcing.logging.model.Event

import scala.collection.mutable

/**
	* Created by Oleg Stoliarov on 12/5/18.
	*/
private[eventsourcing] object LoggerManager {
	val LoggerManagerName = "logger-manager"
	val LoggerManagerPath = s"user/$EventLogSupervisorName/$LoggerManagerName"
	val ConsoleLoggerName = "console-logger"
	val ConsoleWriterName = "console-writer"

	def props(initRequestIdNumber: Long): Props = Props(new LoggerManager(initRequestIdNumber))

	case object CreateConsoleLogger

	case object StopAllLoggers

	final case class LogEvent(event: Event)

	final case class LogEventWithRetry(requestId: RequestId, event: Event)

}

private[logger] class LoggerManager(initRequestIdNumber: Long) extends Actor
	with LoggerManagerState
	with LoggerManagerBehavior
	with ActorLogging {

	import LoggerManager._

	override lazy val loggers: Set[ActorRef] = Set(consoleLogger)
	override val failedRequestIds: mutable.Set[RequestId] = mutable.Set.empty
	override val nextRequestIdNumber: AtomicLong = new AtomicLong(initRequestIdNumber)

	override def postStop(): Unit =
		log.error(s"The ids of the failed logging requests: $failedRequestIds")

	private val consoleLogger: ActorRef = {
		val consoleWriter = context.actorOf(ConsoleWriter.props(withFailures = false), ConsoleWriterName)
		val consoleLogger = context.actorOf(Logger.props(writer = consoleWriter), ConsoleLoggerName)
		consoleLogger
	}
}
