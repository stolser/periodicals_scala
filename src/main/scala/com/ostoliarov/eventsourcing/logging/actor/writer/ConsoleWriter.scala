package com.ostoliarov.eventsourcing.logging.actor.writer

import akka.actor.{Actor, ActorLogging, Props}
import com.ostoliarov.eventsourcing.logging._
import com.ostoliarov.eventsourcing.logging.actor.logger.impl.Logger.{LogEventFailure, LogEventSuccess}
import com.ostoliarov.eventsourcing.logging.model.Event

import scala.util.Random

/**
	* Created by Oleg Stoliarov on 12/5/18.
	*/
private[eventsourcing] object ConsoleWriter {
	def props(withFailures: Boolean): Props = Props(new ConsoleWriter(withFailures))

	final case class WriteEvent(requestId: RequestId, event: Event)

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

	def isStatusOk(requestId: RequestId): Boolean = {
		val requestIdNumber = requestId.split('_')(1).toLong
		if (withFailures) {
			if (requestIdNumber % 3 == 0) {
				throw new RuntimeException(s"Actor '$self' failed due an exception...")
			}
			Thread.sleep(1000 * Random.nextInt(5))
			Random.nextBoolean()
		} else if (requestIdNumber > 0) true
		else false
	}

	override def preStart(): Unit = log.info(s"Starting ConsoleWriter '${self.path.name}'...")

	override def postStop(): Unit = log.info(s"Stopping ConsoleWriter '${self.path.name}'...")
}
