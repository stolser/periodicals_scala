package com.ostoliarov.eventsourcing

import akka.actor.{Actor, ActorLogging, Props}
import com.ostoliarov.eventsourcing.logging.actor.logger.LoggerManager
import com.ostoliarov.eventsourcing.logging.actor.logger.LoggerManager.LoggerManagerName

/**
	* Created by Oleg Stoliarov on 12/4/18.
	*/
private[eventsourcing] object EventSourcingSupervisor {
	val eventLogSupervisorName = "top-supervisor"

	def props: Props = Props[EventSourcingSupervisor]

	case object StartLoggerManager

}

private[eventsourcing] class EventSourcingSupervisor extends Actor with ActorLogging {

	context.actorOf(LoggerManager.props, LoggerManagerName)

	println("-----------------------------------------------")

	override def preStart(): Unit = log.info("Starting actor EventSourcingSupervisor...")

	override def postStop(): Unit = log.info("Stopping actor EventSourcingSupervisor...")

	override def receive: Receive = Actor.emptyBehavior
}