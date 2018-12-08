package com.ostoliarov.eventsourcing.logging.actor.logger

import java.io.File
import java.util.concurrent.atomic.AtomicLong

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.ostoliarov.eventsourcing.EventSourcingApp.actorSystem
import com.ostoliarov.eventsourcing.EventSourcingSettings
import com.ostoliarov.eventsourcing.EventSourcingSupervisor.eventLogSupervisorName
import com.ostoliarov.eventsourcing.logging.EventLoggingHelper.propertiesFromFile
import com.ostoliarov.eventsourcing.logging._
import com.ostoliarov.eventsourcing.logging.actor.logger.impl.Logger
import com.ostoliarov.eventsourcing.logging.actor.writer.ConsoleWriter
import com.ostoliarov.eventsourcing.logging.model.Event

import scala.collection.mutable

/**
	* Created by Oleg Stoliarov on 12/5/18.
	*/
private[eventsourcing] object LoggerManager {
	private lazy val properties: Map[String, String] = propertiesFromFile(
		propFile = new File(EventSourcingSettings(actorSystem).pathToLoggerManagerPropFile)
	)
	val initEventUUIDKeyName = "initEventUUID"
	lazy val nextEventUUID = new AtomicLong(properties(initEventUUIDKeyName).toInt)
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
		log.error(s"The ids of the failed logging requests: $failedRequestIds")
		// todo: get current nextEventUuid, and path to the prop file and persist it;
	}

	override def preStart(): Unit = super.preStart()

	private val consoleLogger: ActorRef = {
		val consoleWriter = context.actorOf(ConsoleWriter.props(withFailures = false), ConsoleWriterName)
		val consoleLogger = context.actorOf(Logger.props(writer = consoleWriter), ConsoleLoggerName)
		consoleLogger
	}
}
