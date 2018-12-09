package com.ostoliarov.eventsourcing.logging.actor.writer

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorLogging, Props}
import com.ostoliarov.eventsourcing.EventSourcingApp.actorSystem
import com.ostoliarov.eventsourcing.logging._
import com.ostoliarov.eventsourcing.logging.actor.logger.LoggerManager.{ConsoleLoggerName, LoggerManagerPath}
import com.ostoliarov.eventsourcing.logging.actor.logger.impl.Logger.{LogEventFailure, LogEventSuccess, WriterIsAlive}
import com.ostoliarov.eventsourcing.logging.model.Event

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Random, Success}

/**
	* Created by Oleg Stoliarov on 12/5/18.
	*/
private[eventsourcing] object ConsoleWriter {
	def props(withFailures: Boolean): Props = Props(new ConsoleWriter(withFailures))

	final case class WriteEvent(requestId: RequestId, event: Event)

}

private[eventsourcing] class ConsoleWriter(withFailures: Boolean) extends Actor with ActorLogging {

	import ConsoleWriter._

	implicit private val ec: ExecutionContext = ExecutionContext.global
	private val finiteDuration = FiniteDuration(500, TimeUnit.MILLISECONDS)
	private val consoleLoggerPath = s"$LoggerManagerPath/$ConsoleLoggerName"

	override def preStart(): Unit = {
		log.info(s"Starting ConsoleWriter '${self.path.name}'...")
		notifyLoggerAboutRestart()
	}

	override def postStop(): Unit = log.info(s"Stopping ConsoleWriter '${self.path.name}'...")

	override def receive: Receive = {
		case WriteEvent(requestId, event) =>
			if (checksArePassedAndNoFailures(requestId)) {
				log.info(s"[requestId=$requestId]:${event.eventMessage}")
				sender ! LogEventSuccess(requestId)
			} else sender ! LogEventFailure(requestId)
	}

	private def checksArePassedAndNoFailures(requestId: RequestId): Boolean = {
		val requestIdNumber = requestId.split('_')(1).toLong
		if (withFailures) {
			if (Random.nextBoolean()) {
				throw new RuntimeException(s"Actor '$self' failed due an exception...")
			}
			Thread.sleep(1000 * Random.nextInt(5))
			Random.nextBoolean()
		} else if (requestIdNumber > 0) true
		else false
	}

	private def notifyLoggerAboutRestart(): Unit = {
		actorSystem
			.actorSelection(consoleLoggerPath)
			.resolveOne(finiteDuration)
			.onComplete {
				case Success(consoleLoggerRef) => consoleLoggerRef ! WriterIsAlive
				case Failure(_) => actorSystem.log.error("Unable to look up the Console Logger actor " +
					s"with path '$consoleLoggerPath'")
			}
	}
}
