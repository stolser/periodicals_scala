package com.ostoliarov.eventsourcing.actor.logger

import java.util.concurrent.atomic.AtomicLong

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.ostoliarov.eventsourcing.actor.EventLogSupervisor.EventLogSupervisorName
import com.ostoliarov.eventsourcing.actor.logger.Logger.{LogEventFailure, LogEventSuccess, Stop}
import com.ostoliarov.eventsourcing.actor.writer.ConsoleWriter
import com.ostoliarov.eventsourcing.model.Event

import scala.collection.mutable

/**
	* Created by Oleg Stoliarov on 12/5/18.
	*/
object LoggerManager {
	val LoggerManagerName = "logger-manager"
	val LoggerManagerPath = s"user/$EventLogSupervisorName/$LoggerManagerName"

	private val nextRequestId = new AtomicLong(1)

	def props: Props = Props[LoggerManager]

	case object CreateConsoleLogger

	case object StopAllLoggers

	final case class LogEvent(event: Event)

	final case class LogEventWithRetry(requestId: Long, event: Event)

}

class LoggerManager extends Actor with ActorLogging {

	import LoggerManager._

	private val loggers = mutable.Set[ActorRef]()
	private val loggingFailures = mutable.Set[Long]()

	createConsoleLogger()

	override def postStop(): Unit =
		log.error(s"The ids of failed logging request: $loggingFailures")

	override def receive: Receive = {
		case LogEvent(event) =>
			val requestId = nextRequestId.getAndIncrement()
			loggers.foreach(_ ! LogEventWithRetry(requestId, event))

		case LogEventFailure(requestId) =>
			loggingFailures += requestId

		case LogEventSuccess(requestId) => Actor.emptyBehavior

		case StopAllLoggers => loggers.foreach(_ ! Stop)
	}

	private def createConsoleLogger() = {
		val consoleWriter = context.actorOf(ConsoleWriter.props, "console-writer")
		val consoleLogger = context.actorOf(Logger.props(writer = consoleWriter), "console-logger")
		loggers += consoleLogger
	}
}
