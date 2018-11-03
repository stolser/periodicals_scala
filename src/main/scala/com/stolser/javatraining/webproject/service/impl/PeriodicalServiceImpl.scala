package com.stolser.javatraining.webproject.service.impl

import java.util
import java.util.NoSuchElementException

import com.stolser.javatraining.webproject.connection.pool.{ConnectionPool, ConnectionPoolProvider}
import com.stolser.javatraining.webproject.dao.{DaoFactory, DaoFactoryTrait, PeriodicalDao}
import com.stolser.javatraining.webproject.model.entity.periodical.{Periodical, PeriodicalCategory, PeriodicalStatus}
import com.stolser.javatraining.webproject.model.entity.statistics.PeriodicalNumberByCategory
import com.stolser.javatraining.webproject.model.entity.subscription.SubscriptionStatus
import com.stolser.javatraining.webproject.service.PeriodicalService
import com.stolser.javatraining.webproject.service.ServiceUtils.withConnection

/**
  * Created by Oleg Stoliarov on 10/15/18.
  */
object PeriodicalServiceImpl extends PeriodicalService {
	private val NO_PERIODICAL_WITH_ID_MESSAGE = "There is no periodical in the DB with id = %d"
	private var factory = DaoFactory.getMysqlDaoFactory
	private implicit var implicitConnectionPool: ConnectionPool = ConnectionPoolProvider.getPool

	private[impl] def daoFactory_= (factory: DaoFactoryTrait): Unit = {
		require(factory != null)
		this.factory = factory
	}

	private[impl] def connectionPool_= (connectionPool: ConnectionPool): Unit = {
		require(connectionPool != null)
		implicitConnectionPool = connectionPool
	}

	override def findOneById(id: Long): Periodical =
		withConnection { conn =>
			factory.getPeriodicalDao(conn).findOneById(id)
		}

	override def findOneByName(name: String): Periodical =
		withConnection { conn =>
			factory.getPeriodicalDao(conn).findOneByName(name)
		}

	override def findAll: util.List[Periodical] =
		withConnection { conn =>
			factory.getPeriodicalDao(conn).findAll
		}

	override def findAllByStatus(status: PeriodicalStatus.Value): util.List[Periodical] =
		withConnection { conn =>
			factory.getPeriodicalDao(conn).findAllByStatus(status)
		}

	override def save(periodical: Periodical): Periodical = {
		if (periodical.id == 0)
			createNewPeriodical(periodical)
		else
			updatePeriodical(periodical)

		getPeriodicalFromDbByName(periodical.name)
	}

	private def createNewPeriodical(periodical: Periodical): Unit =
		withConnection { conn =>
			factory.getPeriodicalDao(conn).createNew(periodical)
		}

	private def getPeriodicalFromDbByName(name: String): Periodical =
		withConnection { conn =>
			factory.getPeriodicalDao(conn).findOneByName(name)
		}

	private def updatePeriodical(periodical: Periodical): Unit =
		withConnection { conn =>
			val affectedRows = factory.getPeriodicalDao(conn).update(periodical)

			if (affectedRows == 0)
				throw new NoSuchElementException(NO_PERIODICAL_WITH_ID_MESSAGE.format(periodical.id))
		}

	override def updateAndSetDiscarded(periodical: Periodical): Int =
		withConnection { conn =>
			factory.getPeriodicalDao(conn).updateAndSetDiscarded(periodical)
		}

	override def deleteAllDiscarded(): Int =
		withConnection { conn =>
			factory.getPeriodicalDao(conn).deleteAllDiscarded()
		}

	override def hasActiveSubscriptions(periodicalId: Long): Boolean =
		withConnection { conn =>
			!factory.getSubscriptionDao(conn)
				.findAllByPeriodicalIdAndStatus(periodicalId, SubscriptionStatus.ACTIVE)
				.isEmpty
		}

	override def getQuantitativeStatistics: util.List[PeriodicalNumberByCategory] =
		withConnection { conn =>
			val statistics = new util.ArrayList[PeriodicalNumberByCategory]
			val dao = factory.getPeriodicalDao(conn)

			for (category <- PeriodicalCategory.values) {
				statistics.add(getPeriodicalNumberByCategory(dao, category))
			}

			statistics
		}

	private def getPeriodicalNumberByCategory(dao: PeriodicalDao,
											  category: PeriodicalCategory): PeriodicalNumberByCategory = {
		val activeNumber = dao.findNumberOfPeriodicalsWithCategoryAndStatus(category,
			PeriodicalStatus.ACTIVE)
		val inActiveNumber = dao.findNumberOfPeriodicalsWithCategoryAndStatus(category,
			PeriodicalStatus.INACTIVE)
		val discardedNumber = dao.findNumberOfPeriodicalsWithCategoryAndStatus(category,
			PeriodicalStatus.DISCARDED)

		PeriodicalNumberByCategory(
			category = Some(category),
			active = activeNumber,
			inActive = inActiveNumber,
			discarded = discardedNumber
		)
	}
}
