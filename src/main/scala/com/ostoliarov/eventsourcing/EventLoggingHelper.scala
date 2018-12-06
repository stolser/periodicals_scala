package com.ostoliarov.eventsourcing

import java.util.concurrent.TimeUnit

import com.ostoliarov.eventsourcing.EventSourcingApp.actorSystem
import com.ostoliarov.eventsourcing.actor.logger.LoggerManager.{LogEvent, LoggerManagerPath}
import com.ostoliarov.eventsourcing.model.Event

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success}

/**
	* Created by Oleg Stoliarov on 12/6/18.
	*/
object EventLoggingHelper {
	implicit private val ec: ExecutionContext = ExecutionContext.global
	private val finiteDuration = FiniteDuration(500, TimeUnit.MILLISECONDS)

	def logEvent(event: Event): Unit =
		actorSystem
			.actorSelection(LoggerManagerPath)
			.resolveOne(finiteDuration)
			.onComplete {
				case Success(loggerManagerRef) => loggerManagerRef ! LogEvent(event)
				case Failure(_) => actorSystem.log.error("Unable to look up the Logger Manager actor " +
					s"with name = '$LoggerManagerPath'")
			}
}
