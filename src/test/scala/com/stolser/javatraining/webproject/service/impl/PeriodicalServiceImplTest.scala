package com.stolser.javatraining.webproject.service.impl

import java.util.NoSuchElementException

import com.stolser.javatraining.webproject.FunSuiteMockitoScalaBase
import com.stolser.javatraining.webproject.connection.pool.ConnectionPool
import com.stolser.javatraining.webproject.dao._
import com.stolser.javatraining.webproject.model.entity.periodical.Periodical
import com.stolser.javatraining.webproject.model.entity.subscription.Subscription
import com.stolser.javatraining.webproject.model.entity.subscription.SubscriptionStatus._
import com.stolser.javatraining.webproject.service.PeriodicalService

/**
	* Created by Oleg Stoliarov on 11/7/18.
	*/
class PeriodicalServiceImplTest extends FunSuiteMockitoScalaBase {
	private var daoFactoryMock: DaoFactory = _
	private var connectionPoolMock: ConnectionPool = _
	private var conn: AbstractConnection = _
	private var periodicalDao: PeriodicalDao = _
	private var subscriptionDao: SubscriptionDao = _
	private var periodicalServiceImpl: PeriodicalService = _

	before {
		daoFactoryMock = mock[DaoFactory]
		connectionPoolMock = mock[ConnectionPool]
		conn = mock[AbstractConnection]
		periodicalDao = mock[PeriodicalDao]
		subscriptionDao = mock[SubscriptionDao]
		periodicalServiceImpl = new PeriodicalServiceImpl with ServiceDependency {
			override implicit val connectionPool: ConnectionPool = connectionPoolMock
			override val daoFactory: DaoFactory = daoFactoryMock
		}
	}

	test("findOneByName() Should return the correct periodical") {
		val periodicalName = "Test Periodical"
		val expectedPeriodical = Some(Periodical(id = 7, name = "Test Periodical", publisher = "Test Publisher"))

		when(connectionPoolMock.connection) thenReturn conn
		when(daoFactoryMock.periodicalDao(any[AbstractConnection])) thenReturn periodicalDao
		when(periodicalDao.findOneByName(periodicalName)) thenReturn expectedPeriodical

		val actualPeriodical = periodicalServiceImpl.findOneByName(periodicalName)

		verify(conn).close()

		assert(actualPeriodical === expectedPeriodical)
	}

	test("save() Should create a new periodical if id == 0") {
		val periodicalName = "Periodical A"
		val newPeriodicalIdInDb = 7
		val periodicalToSave = Periodical(id = 0, name = periodicalName)
		val savedPeriodical = Some(Periodical(id = newPeriodicalIdInDb, name = periodicalName))

		when(connectionPoolMock.connection) thenReturn conn
		when(daoFactoryMock.periodicalDao(any[AbstractConnection])) thenReturn periodicalDao
		when(periodicalDao.findOneByName(periodicalName)) thenReturn savedPeriodical

		val actualPeriodical = periodicalServiceImpl.save(periodicalToSave)

		assert(actualPeriodical === savedPeriodical.get)

		verify(periodicalDao).createNew(periodicalToSave)
		verify(periodicalDao).findOneByName(periodicalName)
	}

	test("save() Should update an existing periodical if id != 0") {
		val periodicalName = "Test Periodical"
		val periodicalToSave = Some(Periodical(id = 5, name = periodicalName))
		val updatedRowsNumber = 1

		when(connectionPoolMock.connection) thenReturn conn
		when(daoFactoryMock.periodicalDao(any[AbstractConnection])) thenReturn periodicalDao
		when(periodicalDao.findOneByName(periodicalName)) thenReturn periodicalToSave
		when(periodicalDao.update(periodicalToSave.get)) thenReturn updatedRowsNumber

		val actualPeriodical = periodicalServiceImpl.save(periodicalToSave.get)

		assert(actualPeriodical === periodicalToSave.get)

		verify(periodicalDao).update(periodicalToSave.get)
		verify(periodicalDao).findOneByName(periodicalName)
	}

	test("save() Should throw NoSuchElementException if no rows were updated by the query") {
		when(connectionPoolMock.connection) thenReturn conn
		when(daoFactoryMock.periodicalDao(any[AbstractConnection])) thenReturn periodicalDao
		when(periodicalDao.update(any[Periodical])) thenReturn 0

		assertThrows[NoSuchElementException] {
			periodicalServiceImpl.save(Periodical(id = 5))
		}
	}

	test("hasActiveSubscriptions() Should return 'false' if there is no ACTIVE subscriptions on his periodical") {
		val periodicalId = 10

		when(connectionPoolMock.connection) thenReturn conn
		when(daoFactoryMock.subscriptionDao(any[AbstractConnection])) thenReturn subscriptionDao
		when(subscriptionDao.findAllByPeriodicalIdAndStatus(periodicalId, ACTIVE))
			.thenReturn(List.empty[Subscription])

		assert(!periodicalServiceImpl.hasActiveSubscriptions(periodicalId))
	}
}
