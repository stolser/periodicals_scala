package com.ostoliarov.eventsourcing

import akka.actor.{Actor, ActorLogging, Props}
import com.ostoliarov.eventsourcing.logging.actor.logger.LoggerManager
import com.ostoliarov.eventsourcing.logging.actor.logger.LoggerManager.LoggerManagerName

/**
	* Created by Oleg Stoliarov on 12/4/18.
	*/
private[eventsourcing] object EventSourcingSupervisor {
	def props: Props = Props[EventSourcingSupervisor]

	case object StartLoggerManager

}

private[eventsourcing] class EventSourcingSupervisor extends Actor with ActorLogging {

	override def preStart(): Unit = {
		log.info("Starting actor EventSourcingSupervisor...")
		context.actorOf(LoggerManager.props, LoggerManagerName)
	}

	override def postStop(): Unit = log.info("Stopping actor EventSourcingSupervisor...")

	override def receive: Receive = Actor.emptyBehavior
}