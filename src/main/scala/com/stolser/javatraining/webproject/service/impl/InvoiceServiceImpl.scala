package com.stolser.javatraining.webproject.service.impl

import java.time.{Instant, LocalDateTime, ZoneId, ZoneOffset}
import java.util
import java.util.List
import java.util.Objects.isNull

import com.stolser.javatraining.webproject.connection.pool.{ConnectionPool, ConnectionPoolProvider}
import com.stolser.javatraining.webproject.dao.{AbstractConnection, DaoFactory, InvoiceDao, SubscriptionDao}
import com.stolser.javatraining.webproject.model.entity.invoice.{Invoice, InvoiceStatus}
import com.stolser.javatraining.webproject.model.entity.periodical.Periodical
import com.stolser.javatraining.webproject.model.entity.statistics.FinancialStatistics
import com.stolser.javatraining.webproject.model.entity.subscription.{Subscription, SubscriptionStatus}
import com.stolser.javatraining.webproject.model.entity.user.User
import com.stolser.javatraining.webproject.service.ServiceUtils.withAbstractConnectionResource
import com.stolser.javatraining.webproject.service.{InvoiceService, ServiceUtils}
import com.stolser.javatraining.webproject.utils.TryWithResources
import com.stolser.javatraining.webproject.utils.TryWithResources.withResources

/**
  * Created by Oleg Stoliarov on 10/15/18.
  */
object InvoiceServiceImpl extends InvoiceService {
	private lazy val factory = DaoFactory.getMysqlDaoFactory
	private implicit lazy val connectionPool: ConnectionPool = ConnectionPoolProvider.getPool

	override def findOneById(invoiceId: Long): Invoice =
		withAbstractConnectionResource { conn =>
			factory.getInvoiceDao(conn).findOneById(invoiceId)
		}

	override def findAllByUserId(userId: Long): util.List[Invoice] =
		withAbstractConnectionResource { conn =>
			factory.getInvoiceDao(conn).findAllByUserId(userId)
		}

	override def findAllByPeriodicalId(periodicalId: Long): util.List[Invoice] =
		withAbstractConnectionResource { conn =>
			factory.getInvoiceDao(conn).findAllByPeriodicalId(periodicalId)
		}

	override def createNew(invoice: Invoice): Unit =
		withAbstractConnectionResource { conn =>
			factory.getInvoiceDao(conn).createNew(invoice)
		}

	override def payInvoice(invoiceToPay: Invoice): Boolean =
		withAbstractConnectionResource { conn =>
			val subscriptionDao = factory.getSubscriptionDao(conn)
			invoiceToPay.status = InvoiceStatus.PAID
			invoiceToPay.paymentDate = Some(Instant.now)

			conn.beginTransaction()
			val userFromDb = factory.getUserDao(conn).findOneById(invoiceToPay.user.getId)
			val periodical = invoiceToPay.periodical

			val existingSubscription = subscriptionDao
				.findOneByUserIdAndPeriodicalId(userFromDb.getId, periodical.getId)

			factory.getInvoiceDao(conn).update(invoiceToPay)

			val subscriptionPeriod = invoiceToPay.subscriptionPeriod

			if (isNull(existingSubscription))
				createAndPersistNewSubscription(userFromDb, periodical, subscriptionPeriod, subscriptionDao)
			else
				updateExistingSubscription(existingSubscription, subscriptionPeriod, subscriptionDao)

			conn.commitTransaction()
			return true
		}

	private def updateExistingSubscription(existingSubscription: Subscription,
										   subscriptionPeriod: Int,
										   subscriptionDao: SubscriptionDao): Unit = {
		val newEndDate =
			if (SubscriptionStatus.INACTIVE == existingSubscription.getStatus)
				getEndDate(Some(Instant.now), subscriptionPeriod)
			else
				getEndDate(existingSubscription.getEndDate, subscriptionPeriod)

		existingSubscription.endDate = newEndDate
		existingSubscription.status = SubscriptionStatus.ACTIVE
		subscriptionDao.update(existingSubscription)
	}

	private def createAndPersistNewSubscription(userFromDb: User,
												periodical: Periodical,
												subscriptionPeriod: Int,
												subscriptionDao: SubscriptionDao): Unit =
		subscriptionDao.createNew(Subscription(
			user = userFromDb,
			periodical = periodical,
			deliveryAddress = userFromDb.getAddress.getOrElse(""),
			endDate = getEndDate(Some(Instant.now), subscriptionPeriod),
			status = SubscriptionStatus.ACTIVE
		))

	private def getEndDate(startDate: Option[Instant],
						   subscriptionPeriod: Int) =
		startDate match {
			case Some(date) =>
				val endDate = LocalDateTime.ofInstant(date, ZoneId.systemDefault)
				Some(endDate
					.plusMonths(subscriptionPeriod)
					.toInstant(ZoneOffset.UTC))
			case None => None
		}

	override def getFinStatistics(since: Instant, until: Instant): FinancialStatistics =
		withAbstractConnectionResource { conn =>
			val dao = factory.getInvoiceDao(conn)
			val totalInvoiceSum = dao.getCreatedInvoiceSumByCreationDate(since, until)
			val paidInvoiceSum = dao.getPaidInvoiceSumByPaymentDate(since, until)

			FinancialStatistics(totalInvoiceSum, paidInvoiceSum)
		}
}
