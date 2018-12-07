package com.ostoliarov.eventsourcing.logging.model

import java.time.Instant

/**
	* Created by Oleg Stoliarov on 12/5/18.
	*/
trait Event {
	val uuid: Long
	val time: Instant
	val userId: Long

	def eventMessage: String
}
