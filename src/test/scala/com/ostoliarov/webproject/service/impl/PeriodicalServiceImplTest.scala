package com.ostoliarov.webproject.service.impl

import java.util.NoSuchElementException

import com.ostoliarov.webproject.FunSuiteWithMockitoScalaBase
import com.ostoliarov.webproject.connection.AbstractConnection
import com.ostoliarov.webproject.connection.pool.ConnectionPool
import com.ostoliarov.webproject.dao._
import com.ostoliarov.webproject.model.entity.periodical.Periodical
import com.ostoliarov.webproject.model.entity.subscription.Subscription
import com.ostoliarov.webproject.model.entity.subscription.SubscriptionStatus._
import com.ostoliarov.webproject.service.PeriodicalService

/**
	* Created by Oleg Stoliarov on 11/7/18.
	*/
class PeriodicalServiceImplTest extends FunSuiteWithMockitoScalaBase {
	private var daoFactoryMock: DaoFactory = _
	private var connectionPoolMock: ConnectionPool = _
	private var connMock: AbstractConnection = _
	private var periodicalDaoMock: PeriodicalDao = _
	private var subscriptionDaoMock: SubscriptionDao = _
	private var periodicalServiceImpl: PeriodicalService = _

	before {
		daoFactoryMock = mock[DaoFactory]
		connectionPoolMock = mock[ConnectionPool]
		connMock = mock[AbstractConnection]
		periodicalDaoMock = mock[PeriodicalDao]
		subscriptionDaoMock = mock[SubscriptionDao]
		periodicalServiceImpl = new PeriodicalServiceImpl with ServiceDependency {
			override implicit val connectionPool: ConnectionPool = connectionPoolMock
			override val daoFactory: DaoFactory = daoFactoryMock
		}
	}

	test("findOneByName() Should return the correct periodical") {
		val periodicalName = "Test Periodical"
		val expectedPeriodical = Some(Periodical(id = 7, name = "Test Periodical", publisher = "Test Publisher"))

		when(connectionPoolMock.connection) thenReturn connMock
		when(daoFactoryMock.periodicalDao(any[AbstractConnection])) thenReturn periodicalDaoMock
		when(periodicalDaoMock.findOneByName(periodicalName)) thenReturn expectedPeriodical

		val actualPeriodical = periodicalServiceImpl.findOneByName(periodicalName)

		verify(connMock).close()

		assert(actualPeriodical === expectedPeriodical)
	}

	test("save() Should create a new periodical if id == 0") {
		val zeroId = 0
		val periodicalName = "Periodical A"
		val newPeriodicalIdInDb = 7
		val periodicalToSave = Periodical(id = zeroId, name = periodicalName)
		val savedPeriodical = Some(Periodical(id = newPeriodicalIdInDb, name = periodicalName))

		when(connectionPoolMock.connection) thenReturn connMock
		when(daoFactoryMock.periodicalDao(any[AbstractConnection])) thenReturn periodicalDaoMock
		when(periodicalDaoMock.findOneByName(periodicalName)) thenReturn savedPeriodical

		val actualPeriodical = periodicalServiceImpl.save(periodicalToSave)

		assert(actualPeriodical === savedPeriodical.get)

		verify(periodicalDaoMock).createNew(periodicalToSave)
		verify(periodicalDaoMock).findOneByName(periodicalName)
	}

	test("save() Should update an existing periodical if id != 0") {
		val periodicalName = "Test Periodical"
		val periodicalToSave = Some(Periodical(id = 5, name = periodicalName))
		val updatedRowsNumber = 1

		when(connectionPoolMock.connection) thenReturn connMock
		when(daoFactoryMock.periodicalDao(any[AbstractConnection])) thenReturn periodicalDaoMock
		when(periodicalDaoMock.findOneByName(periodicalName)) thenReturn periodicalToSave
		when(periodicalDaoMock.update(periodicalToSave.get)) thenReturn updatedRowsNumber

		val actualPeriodical = periodicalServiceImpl.save(periodicalToSave.get)

		assert(actualPeriodical === periodicalToSave.get)

		verify(periodicalDaoMock).update(periodicalToSave.get)
		verify(periodicalDaoMock).findOneByName(periodicalName)
	}

	test("save() Should throw NoSuchElementException if no rows were updated by the query") {
		when(connectionPoolMock.connection) thenReturn connMock
		when(daoFactoryMock.periodicalDao(any[AbstractConnection])) thenReturn periodicalDaoMock
		when(periodicalDaoMock.update(any[Periodical])) thenReturn 0

		assertThrows[NoSuchElementException] {
			periodicalServiceImpl.save(Periodical(id = 5))
		}
	}

	test("hasActiveSubscriptions() Should return 'false' if there is no ACTIVE subscriptions on his periodical") {
		val periodicalId = 10

		when(connectionPoolMock.connection) thenReturn connMock
		when(daoFactoryMock.subscriptionDao(any[AbstractConnection])) thenReturn subscriptionDaoMock
		when(subscriptionDaoMock.findAllByPeriodicalIdAndStatus(periodicalId, ACTIVE))
			.thenReturn(List.empty[Subscription])

		assert(!periodicalServiceImpl.hasActiveSubscriptions(periodicalId))
	}
}
