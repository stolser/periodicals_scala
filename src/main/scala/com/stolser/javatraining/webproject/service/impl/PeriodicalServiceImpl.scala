package com.stolser.javatraining.webproject.service.impl

import java.util.NoSuchElementException

import com.stolser.javatraining.webproject.connection.pool.{ConnectionPool, ConnectionPoolProvider}
import com.stolser.javatraining.webproject.dao.{AbstractConnection, DaoFactory, DaoFactoryTrait}
import com.stolser.javatraining.webproject.model.entity.periodical.{Periodical, PeriodicalCategory, PeriodicalStatus}
import com.stolser.javatraining.webproject.model.entity.statistics.PeriodicalNumberByCategory
import com.stolser.javatraining.webproject.model.entity.subscription.SubscriptionStatus
import com.stolser.javatraining.webproject.service.PeriodicalService
import com.stolser.javatraining.webproject.service.ServiceUtils.withConnection

/**
	* Created by Oleg Stoliarov on 10/15/18.
	*/
object PeriodicalServiceImpl extends PeriodicalService {
	private implicit var implicitConnectionPool: ConnectionPool = ConnectionPoolProvider.getPool
	private val NO_PERIODICAL_WITH_ID_MESSAGE = "There is no periodical in the DB with id = %d"
	private var factory = DaoFactory.mysqlDaoFactory

	private[impl] def daoFactory = factory

	private[impl] def daoFactory_=(factory: DaoFactoryTrait): Unit = {
		require(factory != null)
		this.factory = factory
	}

	private[impl] def connectionPool = implicitConnectionPool

	private[impl] def connectionPool_=(connectionPool: ConnectionPool): Unit = {
		require(connectionPool != null)
		implicitConnectionPool = connectionPool
	}

	override def findOneById(id: Long): Option[Periodical] =
		withConnection { conn =>
			factory.periodicalDao(conn).findOneById(id)
		}

	override def findOneByName(name: String): Option[Periodical] =
		withConnection { conn =>
			factory.periodicalDao(conn).findOneByName(name)
		}

	override def findAll: List[Periodical] =
		withConnection { conn =>
			factory.periodicalDao(conn).findAll
		}

	override def findAllByStatus(status: PeriodicalStatus.Value): List[Periodical] =
		withConnection { conn =>
			factory.periodicalDao(conn).findAllByStatus(status)
		}

	override def save(periodical: Periodical): Periodical = {
		require(periodical != null)

		if (periodical.id == 0)
			createNewPeriodical(periodical)
		else
			updatePeriodical(periodical)

		getPeriodicalFromDbByName(periodical.name)
	}

	private def createNewPeriodical(periodical: Periodical): Unit =
		withConnection { conn =>
			factory.periodicalDao(conn).createNew(periodical)
		}

	private def getPeriodicalFromDbByName(name: String): Periodical =
		withConnection { conn =>
			factory.periodicalDao(conn).findOneByName(name) match {
				case Some(periodical) => periodical
				case None => throw new RuntimeException(s"Periodical with name $name must exist in the db.")
			}
		}

	private def updatePeriodical(periodical: Periodical): Unit =
		withConnection { conn =>
			val affectedRows = factory.periodicalDao(conn).update(periodical)

			if (affectedRows == 0)
				throw new NoSuchElementException(NO_PERIODICAL_WITH_ID_MESSAGE.format(periodical.id))
		}

	override def updateAndSetDiscarded(periodical: Periodical): Int = {
		require(periodical != null)

		withConnection { conn =>
			factory.periodicalDao(conn).updateAndSetDiscarded(periodical)
		}
	}

	override def deleteAllDiscarded(): Int =
		withConnection { conn =>
			factory.periodicalDao(conn).deleteAllDiscarded()
		}

	override def hasActiveSubscriptions(periodicalId: Long): Boolean =
		withConnection { conn =>
			factory.subscriptionDao(conn)
				.findAllByPeriodicalIdAndStatus(periodicalId, SubscriptionStatus.ACTIVE)
				.nonEmpty
		}

	override def quantitativeStatistics: List[PeriodicalNumberByCategory] =
		withConnection { conn =>
			(for (category <- PeriodicalCategory.values)
				yield getPeriodicalNumberByCategory(conn, category))
				.toList
		}

	private def getPeriodicalNumberByCategory(conn: AbstractConnection,
																						category: PeriodicalCategory): PeriodicalNumberByCategory = {
		val dao = factory.periodicalDao(conn)

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