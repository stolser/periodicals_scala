package com.ostoliarov.webproject.model.entity.periodical

/**
	* Created by Oleg Stoliarov on 11/25/18.
	*/
object PeriodicalStatus extends Enumeration {
	type PeriodicalStatus = Value
	val ACTIVE, INACTIVE, DISCARDED = Value
}
