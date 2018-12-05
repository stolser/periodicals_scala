package com.ostoliarov.eventsourcing

import akka.actor.ActorSystem
import com.ostoliarov.eventsourcing.actor.EventLogSupervisor
import com.ostoliarov.eventsourcing.actor.EventLogSupervisor.EventLogSupervisorName

/**
	* Created by Oleg Stoliarov on 12/4/18.
	*/
object EventSourcingApp {
	var actorSystem: ActorSystem = _

	def create(actorSystemName: String): EventSourcingApp[Stopped] =
		new EventSourcingApp[Stopped](actorSystemName)
}

class EventSourcingApp[State <: EventSourcingAppState] private(actorSystemName: String) {

	import EventSourcingApp._

	def start[T >: State <: Stopped](): EventSourcingApp[Started] = {
		println(s"Starting actor system '$actorSystemName'...")
		actorSystem = ActorSystem(actorSystemName)

		actorSystem.actorOf(EventLogSupervisor.props, name = EventLogSupervisorName)

		this.asInstanceOf[EventSourcingApp[Started]]
	}

	def stop[T >: State <: Started](): EventSourcingApp[Stopped] = {
		println(s"Terminating actor system '$actorSystemName'...")

		actorSystem.terminate()

		this.asInstanceOf[EventSourcingApp[Stopped]]
	}
}

sealed trait EventSourcingAppState

final class Started extends EventSourcingAppState

final class Stopped extends EventSourcingAppState
