package com.ostoliarov.eventsourcing.logging.model

import com.ostoliarov.webproject.FunSuiteWithMockitoScalaBase
import com.ostoliarov.webproject.model.entity.user.User

/**
	* Created by Oleg Stoliarov on 12/5/18.
	*/
class EventTest extends FunSuiteWithMockitoScalaBase {
	test("Event implementation should be correctly created.") {
		val testUser = User(id = 10, userName = "stolser")
		val signInEvent: Event = SignInEvent(testUser.id, userIp = "10.20.30.40")
		assert(signInEvent.userId === testUser.id)

		val createUserEvent: Event = CreateUserEvent(userId = testUser.id, testUser)
		assert(createUserEvent.userId === testUser.id)

		assert(signInEvent.uuid === createUserEvent.uuid - 1)
		assert(signInEvent.time.compareTo(createUserEvent.time) <= 0)

	}
}
