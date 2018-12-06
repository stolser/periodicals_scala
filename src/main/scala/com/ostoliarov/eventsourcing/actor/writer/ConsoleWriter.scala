package com.ostoliarov.eventsourcing.actor.writer

import akka.actor.{Actor, ActorLogging, Props}
import com.ostoliarov.eventsourcing.actor.logger.Logger.{LogEventFailure, LogEventSuccess}
import com.ostoliarov.eventsourcing.model.Event

import scala.util.Random

/**
	* Created by Oleg Stoliarov on 12/5/18.
	*/
private[eventsourcing] object ConsoleWriter {
	def props(withFailures: Boolean): Props = Props(new ConsoleWriter(withFailures))

	final case class WriteEvent(requestId: Long, event: Event)

}

private[eventsourcing] class ConsoleWriter(withFailures: Boolean) extends Actor with ActorLogging {

	import ConsoleWriter._

	override def receive: Receive = {
		case WriteEvent(requestId, event) =>
			if (isStatusOk(requestId)) {
				log.info(s"[requestId=$requestId]:${event.eventMessage}")
				sender ! LogEventSuccess(requestId)
			} else sender ! LogEventFailure(requestId)
	}

	def isStatusOk(requestId: Long): Boolean =
		if (withFailures)
			Random.nextBoolean()
		else true

	override def preStart(): Unit = log.info(s"Starting ConsoleWriter '${self.path.name}'...")

	override def postStop(): Unit = log.info(s"Stopping ConsoleWriter '${self.path.name}'...")
}
