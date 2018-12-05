package com.ostoliarov.eventsourcing

import akka.actor.ActorSystem
import akka.testkit.TestKit
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
	* Created by Oleg Stoliarov on 12/5/18.
	*/
class WordSpecAkkaTestKit extends TestKit(ActorSystem("TestActorSystem"))
	with Matchers
	with WordSpecLike
	with BeforeAndAfterAll