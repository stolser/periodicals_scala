package com.ostoliarov.eventsourcing.logging

import java.io.{BufferedWriter, File, FileWriter}
import java.util.concurrent.TimeUnit

import com.ostoliarov.eventsourcing.EventSourcingApp.actorSystem
import com.ostoliarov.eventsourcing.EventSourcingSettings
import com.ostoliarov.eventsourcing.logging.actor.logger.LoggerManager
import com.ostoliarov.eventsourcing.logging.actor.logger.LoggerManager.{LogEvent, LoggerManagerPath}
import com.ostoliarov.eventsourcing.logging.model.Event
import com.ostoliarov.webproject.withResources

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.io.Source
import scala.util.{Failure, Success}

/**
	* Created by Oleg Stoliarov on 12/6/18.
	*/
object EventLoggingUtils {
	implicit private val ec: ExecutionContext = ExecutionContext.global
	private val finiteDuration = FiniteDuration(500, TimeUnit.MILLISECONDS)
	private val keyValueSeparator = '='

	def logEvent(event: Event): Unit =
		actorSystem
			.actorSelection(LoggerManagerPath)
			.resolveOne(finiteDuration)
			.onComplete {
				case Success(loggerManagerRef) => loggerManagerRef ! LogEvent(event)
				case Failure(_) => actorSystem.log.error("Unable to look up the Logger Manager actor " +
					s"with path '$LoggerManagerPath'")
			}

	def readPropertiesFromFile(propFile: File): Map[String, String] = withResources(Source.fromFile(propFile)) {
		source => {
			source.getLines()
				.filter(_ != "")
				.map(line => {
					val Array(key, value) = line.split(keyValueSeparator).map(_.trim)
					key -> value
				})
				.toMap
		}
	}

	def initEventUUID: Int = readPropertiesFromFile(
		propFile = new File(EventSourcingSettings(actorSystem).pathToLoggerManagerPropFile)
	)(LoggerManager.initEventUUIDKeyName).toInt

	def writePropertiesToFile(props: Map[String, String]): Unit = {
		println(s"Persisting properties to a file: props=$props")
		val propsAsString = props
			.map({ case (key, value) => s"$key$keyValueSeparator$value\n" })
			.mkString("")

		val file = new File(EventSourcingSettings(actorSystem).pathToLoggerManagerPropFile)

		val bw = new BufferedWriter(new FileWriter(file))
		bw.write(propsAsString)
		bw.close()
	}
}
