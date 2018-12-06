package com.ostoliarov.eventsourcing.service

import com.ostoliarov.eventsourcing.model.Event

/**
	* Created by Oleg Stoliarov on 12/5/18.
	*/
private[eventsourcing] trait EventService {
	def writeEvent(event: Event)
}
