package com.ostoliarov.eventsourcing.logging.actor.logger

import akka.actor.ActorRef
import com.ostoliarov.eventsourcing.logging._
import com.ostoliarov.eventsourcing.logging.model.Event

import scala.collection.mutable

/**
	* Created by Oleg Stoliarov on 12/7/18.
	*/
trait LoggerState {
	type Retry = Int
	val writer: ActorRef
	val requestRetries: mutable.Map[RequestId, Retry]
	val requestId2Events: mutable.Map[RequestId, Event]
	val RetryNumberLimit: Retry
}
