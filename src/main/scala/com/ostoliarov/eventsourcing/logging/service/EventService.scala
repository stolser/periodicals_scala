package com.ostoliarov.eventsourcing.logging.service

import com.ostoliarov.eventsourcing.logging.model.Event

/**
	* Created by Oleg Stoliarov on 12/5/18.
	*/
private[eventsourcing] trait EventService {
	def writeEvent(event: Event)
}
