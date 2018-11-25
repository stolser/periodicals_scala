package com.ostoliarov.webproject.model.entity.user

/**
	* Created by Oleg Stoliarov on 11/25/18.
	*/
object UserStatus extends Enumeration {
	type UserStatus = Value
	val ACTIVE, BLOCKED = Value
}
