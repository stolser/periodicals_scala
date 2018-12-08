package com.ostoliarov.eventsourcing

import akka.actor.ActorSystem

/**
	* Created by Oleg Stoliarov on 12/4/18.
	*/
object EventSourcingApp {
	val actorSystemName = "periodicals-event-sourcing"
	val eventLogSupervisorName = "top-supervisor"

	println(s"------- Starting actor system '$actorSystemName'...")

	private[eventsourcing] val actorSystem: ActorSystem = ActorSystem(actorSystemName)

	actorSystem.actorOf(EventSourcingSupervisor.props, name = eventLogSupervisorName)

	def stop(): Unit = {
		println(s"------- Terminating actor system '$actorSystemName'...")

		actorSystem.terminate()
	}
}