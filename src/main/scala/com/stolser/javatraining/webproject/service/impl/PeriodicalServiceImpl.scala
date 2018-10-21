package com.stolser.javatraining.webproject.service.impl

import java.util
import java.util.{ArrayList, List, NoSuchElementException}

import com.stolser.javatraining.webproject.connection.pool.{ConnectionPool, ConnectionPoolProvider}
import com.stolser.javatraining.webproject.dao.{AbstractConnection, DaoFactory, PeriodicalDao}
import com.stolser.javatraining.webproject.model.entity.periodical.{Periodical, PeriodicalCategory, PeriodicalStatus}
import com.stolser.javatraining.webproject.model.entity.statistics.PeriodicalNumberByCategory
import com.stolser.javatraining.webproject.model.entity.subscription.{Subscription, SubscriptionStatus}
import com.stolser.javatraining.webproject.service.ServiceUtils.withAbstractConnectionResource
import com.stolser.javatraining.webproject.service.{PeriodicalService, ServiceUtils}

/**
  * Created by Oleg Stoliarov on 10/15/18.
  */
object PeriodicalServiceImpl extends PeriodicalService {
	private val NO_PERIODICAL_WITH_ID_MESSAGE = "There is no periodical in the DB with id = %d"
	private lazy val factory = DaoFactory.getMysqlDaoFactory
	private implicit lazy val connectionPool: ConnectionPool = ConnectionPoolProvider.getPool

	override def findOneById(id: Long): Periodical =
		withAbstractConnectionResource { conn =>
			factory.getPeriodicalDao(conn).findOneById(id)
		}

	override def findOneByName(name: String): Periodical =
		withAbstractConnectionResource { conn =>
			factory.getPeriodicalDao(conn).findOneByName(name)
		}

	override def findAll: util.List[Periodical] =
		withAbstractConnectionResource { conn =>
			factory.getPeriodicalDao(conn).findAll
		}

	override def findAllByStatus(status: PeriodicalStatus.Value): util.List[Periodical] =
		withAbstractConnectionResource { conn =>
			factory.getPeriodicalDao(conn).findAllByStatus(status)
		}

	override def save(periodical: Periodical): Periodical = {
		if (periodical.getId == 0)
			createNewPeriodical(periodical)
		else
			updatePeriodical(periodical)

		getPeriodicalFromDbByName(periodical.getName)
	}

	private def createNewPeriodical(periodical: Periodical): Unit =
		withAbstractConnectionResource { conn =>
			factory.getPeriodicalDao(conn).createNew(periodical)
		}

	private def getPeriodicalFromDbByName(name: String): Periodical =
		withAbstractConnectionResource { conn =>
			factory.getPeriodicalDao(conn).findOneByName(name)
		}

	private def updatePeriodical(periodical: Periodical): Unit =
		withAbstractConnectionResource { conn =>
			val affectedRows = factory.getPeriodicalDao(conn).update(periodical)

			if (affectedRows == 0)
				throw new NoSuchElementException(NO_PERIODICAL_WITH_ID_MESSAGE.format(periodical.getId))
		}

	override def updateAndSetDiscarded(periodical: Periodical): Int =
		withAbstractConnectionResource { conn =>
			factory.getPeriodicalDao(conn).updateAndSetDiscarded(periodical)
		}

	override def deleteAllDiscarded(): Int =
		withAbstractConnectionResource { conn =>
			factory.getPeriodicalDao(conn).deleteAllDiscarded()
		}

	override def hasActiveSubscriptions(periodicalId: Long): Boolean =
		withAbstractConnectionResource { conn =>
			!factory.getSubscriptionDao(conn)
				.findAllByPeriodicalIdAndStatus(periodicalId, SubscriptionStatus.ACTIVE)
				.isEmpty
		}

	override def getQuantitativeStatistics: util.List[PeriodicalNumberByCategory] =
		withAbstractConnectionResource { conn =>
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
			category = category,
			active = activeNumber,
			inActive = inActiveNumber,
			discarded = discardedNumber
		)
	}
}
