package com.ostoliarov.eventsourcing

import akka.actor.{Actor, ActorLogging, Props}
import com.ostoliarov.eventsourcing.logging.actor.logger.LoggerManager
import com.ostoliarov.eventsourcing.logging.actor.logger.LoggerManager.LoggerManagerName

/**
	* Created by Oleg Stoliarov on 12/4/18.
	*/
private[eventsourcing] object EventSourcingSupervisor {
	val EventLogSupervisorName = "top-supervisor"
	private val LoggerManagerInitRequestId = 1

	def props: Props = Props[EventSourcingSupervisor]

	case object StartLoggerManager

}

private[eventsourcing] class EventSourcingSupervisor extends Actor with ActorLogging {

	import EventSourcingSupervisor._

	context.actorOf(LoggerManager.props(LoggerManagerInitRequestId), LoggerManagerName)

	override def preStart(): Unit = log.info("Starting actor EventSourcingSupervisor...")

	override def postStop(): Unit = log.info("Stopping actor EventSourcingSupervisor...")

	override def receive: Receive = Actor.emptyBehavior
}
