package com.ostoliarov.eventsourcing.logging.actor.logger

import java.io.File
import java.util.concurrent.atomic.AtomicLong

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.ostoliarov.eventsourcing.EventSourcingApp.{actorSystem, eventLogSupervisorName}
import com.ostoliarov.eventsourcing.EventSourcingSettings
import com.ostoliarov.eventsourcing.logging.EventLoggingUtils._
import com.ostoliarov.eventsourcing.logging._
import com.ostoliarov.eventsourcing.logging.actor.logger.impl.Logger
import com.ostoliarov.eventsourcing.logging.actor.writer.ConsoleWriter
import com.ostoliarov.eventsourcing.logging.model.Event

import scala.collection.mutable

/**
	* Created by Oleg Stoliarov on 12/5/18.
	*/
private[eventsourcing] object LoggerManager {
	private var properties: Map[String, String] = readPropertiesFromFile(
		propFile = new File(EventSourcingSettings(actorSystem).pathToLoggerManagerPropFile)
	)
	val initEventUUIDKeyName = "initEventUUID"
	lazy val nextEventUUID = new AtomicLong(initEventUUID)
	val LoggerManagerName = "logger-manager"
	val LoggerManagerPath = s"user/$eventLogSupervisorName/$LoggerManagerName"
	val ConsoleLoggerName = "console-logger"
	val ConsoleWriterName = "console-writer"

	def props: Props = Props[LoggerManager]

	case object CreateConsoleLogger

	case object StopAllLoggers

	final case class LogEvent(event: Event)

	final case class LogEventWithRetry(requestId: RequestId, event: Event)

}

private[logger] class LoggerManager extends Actor
	with LoggerManagerState
	with LoggerManagerBehavior
	with ActorLogging {

	import LoggerManager._

	override lazy val loggers: Set[ActorRef] = Set(consoleLogger)
	override val failedRequestIds: mutable.Set[RequestId] = mutable.Set.empty

	override def postStop(): Unit = {
		log.info(s"The ids of the failed logging requests: $failedRequestIds")
		persistCurrentState()
	}

	private def persistCurrentState(): Unit = {
		properties += (initEventUUIDKeyName -> nextEventUUID.get().toString)
		writePropertiesToFile(properties)
	}

	private val consoleLogger: ActorRef = {
		val consoleWriter = context.actorOf(ConsoleWriter.props(withFailures = true), ConsoleWriterName)
		val consoleLogger = context.actorOf(Logger.props(writer = consoleWriter), ConsoleLoggerName)
		consoleLogger
	}
}
