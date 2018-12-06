package com.ostoliarov.eventsourcing.actor

import akka.actor.{Actor, ActorLogging, Props}
import com.ostoliarov.eventsourcing.actor.logger.LoggerManager
import com.ostoliarov.eventsourcing.actor.logger.LoggerManager.LoggerManagerName

/**
	* Created by Oleg Stoliarov on 12/4/18.
	*/
private[eventsourcing] object EventLogSupervisor {
	val EventLogSupervisorName = "event-log-supervisor"

	def props: Props = Props[EventLogSupervisor]

	case object StartLoggerManager

}

private[eventsourcing] class EventLogSupervisor extends Actor with ActorLogging {

	context.actorOf(LoggerManager.props, LoggerManagerName)

	override def preStart(): Unit = log.info("Starting actor EventLogSupervisor...")

	override def postStop(): Unit = log.info("Stopping actor EventLogSupervisor...")

	override def receive: Receive = Actor.emptyBehavior
}
