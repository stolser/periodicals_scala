package com.ostoliarov.eventsourcing.logging.actor.logger

import akka.actor.ActorRef
import com.ostoliarov.eventsourcing.logging._

import scala.collection.mutable

/**
	* Created by Oleg Stoliarov on 12/7/18.
	*/
trait LoggerManagerState {
	val loggers: Set[ActorRef]
	val failedRequestIds: mutable.Set[RequestId]
}
