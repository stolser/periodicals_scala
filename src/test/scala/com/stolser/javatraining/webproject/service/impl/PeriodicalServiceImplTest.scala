package com.stolser.javatraining.webproject.service.impl

import java.util.NoSuchElementException

import com.stolser.javatraining.webproject.FunSuiteBase
import com.stolser.javatraining.webproject.connection.pool.ConnectionPool
import com.stolser.javatraining.webproject.dao._
import com.stolser.javatraining.webproject.model.entity.periodical.Periodical
import com.stolser.javatraining.webproject.model.entity.subscription.Subscription
import com.stolser.javatraining.webproject.model.entity.subscription.SubscriptionStatus._

/**
	* Created by Oleg Stoliarov on 11/7/18.
	*/
class PeriodicalServiceImplTest extends FunSuiteBase {
	private var daoFactory: DaoFactoryTrait = _
	private var connectionPool: ConnectionPool = _
	private var conn: AbstractConnection = _
	private var periodicalDao: PeriodicalDao = _
	private var subscriptionDao: SubscriptionDao = _

	before {
		daoFactory = mock[DaoFactoryTrait]
		connectionPool = mock[ConnectionPool]
		conn = mock[AbstractConnection]
		periodicalDao = mock[PeriodicalDao]
		subscriptionDao = mock[SubscriptionDao]
	}

	test("findOneByName() Should return the correct periodical") {
		val periodicalName = "Test Periodical"
		val expectedPeriodical = Periodical(id = 7, name = "Test Periodical", publisher = "Test Publisher")
		val periodicalServiceImpl = PeriodicalServiceImpl
		periodicalServiceImpl.daoFactory_=(daoFactory)
		periodicalServiceImpl.connectionPool_=(connectionPool)

		when(connectionPool.connection) thenReturn conn
		when(daoFactory.periodicalDao(any[AbstractConnection])) thenReturn periodicalDao
		when(periodicalDao.findOneByName(periodicalName)) thenReturn expectedPeriodical

		val actualPeriodical = periodicalServiceImpl.findOneByName(periodicalName)

		verify(conn).close()

		assert(actualPeriodical === expectedPeriodical)
	}

	test("save() Should create a new periodical if id == 0") {
		val periodicalName = "Periodical A"
		val newPeriodicalIdInDb = 7
		val periodicalToSave = Periodical(id = 0, name = periodicalName)
		val savedPeriodical = Periodical(id = newPeriodicalIdInDb, name = periodicalName)
		val periodicalServiceImpl = PeriodicalServiceImpl
		periodicalServiceImpl.daoFactory_=(daoFactory)
		periodicalServiceImpl.connectionPool_=(connectionPool)

		when(connectionPool.connection) thenReturn conn
		when(daoFactory.periodicalDao(any[AbstractConnection])) thenReturn periodicalDao
		when(periodicalDao.findOneByName(periodicalName)) thenReturn savedPeriodical

		val actualPeriodical = periodicalServiceImpl.save(periodicalToSave)

		assert(actualPeriodical === savedPeriodical)

		verify(periodicalDao).createNew(periodicalToSave)
		verify(periodicalDao).findOneByName(periodicalName)
	}

	test("save() Should update an existing periodical if id != 0") {
		val periodicalName = "Test Periodical"
		val periodicalToSave = Periodical(id = 5, name = periodicalName)
		val updatedRowsNumber = 1
		val periodicalServiceImpl = PeriodicalServiceImpl
		periodicalServiceImpl.daoFactory_=(daoFactory)
		periodicalServiceImpl.connectionPool_=(connectionPool)

		when(connectionPool.connection) thenReturn conn
		when(daoFactory.periodicalDao(any[AbstractConnection])) thenReturn periodicalDao
		when(periodicalDao.findOneByName(periodicalName)) thenReturn periodicalToSave
		when(periodicalDao.update(periodicalToSave)) thenReturn updatedRowsNumber

		val actualPeriodical = periodicalServiceImpl.save(periodicalToSave)

		assert(actualPeriodical === periodicalToSave)

		verify(periodicalDao).update(periodicalToSave)
		verify(periodicalDao).findOneByName(periodicalName)
	}

	test("save() Should throw NoSuchElementException if no rows were updated by the query") {
		val periodicalServiceImpl = PeriodicalServiceImpl
		periodicalServiceImpl.daoFactory_=(daoFactory)
		periodicalServiceImpl.connectionPool_=(connectionPool)

		when(connectionPool.connection) thenReturn conn
		when(daoFactory.periodicalDao(any[AbstractConnection])) thenReturn periodicalDao
		when(periodicalDao.update(any[Periodical])) thenReturn 0

		assertThrows[NoSuchElementException] {
			periodicalServiceImpl.save(Periodical(id = 5))
		}
	}

	test("hasActiveSubscriptions() Should return 'false' if there is no ACTIVE subscriptions on his periodical") {
		val periodicalId = 10
		val periodicalServiceImpl = PeriodicalServiceImpl
		periodicalServiceImpl.daoFactory_=(daoFactory)
		periodicalServiceImpl.connectionPool_=(connectionPool)

		when(connectionPool.connection) thenReturn conn
		when(daoFactory.subscriptionDao(any[AbstractConnection])) thenReturn subscriptionDao
		when(subscriptionDao.findAllByPeriodicalIdAndStatus(periodicalId, ACTIVE))
			.thenReturn(List.empty[Subscription])

		assert(!periodicalServiceImpl.hasActiveSubscriptions(periodicalId))
	}
}
