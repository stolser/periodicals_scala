package com.ostoliarov.eventsourcing

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
	* Created by Oleg Stoliarov on 12/5/18.
	*/
class WordSpecAkkaTestKit extends TestKit(ActorSystem("PeriodicalsActorSystemTest",
	ConfigFactory.parseString("""akka.loggers = ["akka.testkit.TestEventListener"]""")))
	with ImplicitSender
	with Matchers
	with WordSpecLike
	with BeforeAndAfterAll