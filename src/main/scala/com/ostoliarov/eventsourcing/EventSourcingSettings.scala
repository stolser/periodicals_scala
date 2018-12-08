package com.ostoliarov.eventsourcing

import akka.actor.{ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import com.typesafe.config.Config

/**
	* Created by Oleg Stoliarov on 12/8/18.
	*/
object EventSourcingSettings extends ExtensionId[EventSourcingSettingsImpl] with ExtensionIdProvider {
	override def lookup: ExtensionId[_ <: Extension] = EventSourcingSettings

	override def createExtension(system: ExtendedActorSystem): EventSourcingSettingsImpl =
		new EventSourcingSettingsImpl(system.settings.config)
}

class EventSourcingSettingsImpl(config: Config) extends Extension {
	val pathToLoggerManagerPropFile: String =
		config.getString("periodicals.eventsourcing.logging.loggerManager.pathToPropFile")
}
