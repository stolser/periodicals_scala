package com.stolser.javatraining.webproject.service.impl

import java.time.{Instant, LocalDateTime, ZoneId, ZoneOffset}
import java.util
import java.util.List
import java.util.Objects.isNull

import com.stolser.javatraining.webproject.connection.pool.{ConnectionPool, ConnectionPoolProvider}
import com.stolser.javatraining.webproject.dao.{AbstractConnection, DaoFactory, InvoiceDao, SubscriptionDao}
import com.stolser.javatraining.webproject.model.entity.invoice.Invoice
import com.stolser.javatraining.webproject.model.entity.periodical.Periodical
import com.stolser.javatraining.webproject.model.entity.statistics.FinancialStatistics
import com.stolser.javatraining.webproject.model.entity.subscription.Subscription
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
			invoiceToPay.setStatus(Invoice.Status.PAID)
			invoiceToPay.setPaymentDate(Instant.now)

			conn.beginTransaction()
			val userFromDb = factory.getUserDao(conn).findOneById(invoiceToPay.getUser.getId)
			val periodical = invoiceToPay.getPeriodical

			val existingSubscription = subscriptionDao
				.findOneByUserIdAndPeriodicalId(userFromDb.getId, periodical.getId)

			factory.getInvoiceDao(conn).update(invoiceToPay)

			val subscriptionPeriod = invoiceToPay.getSubscriptionPeriod

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
			if (Subscription.Status.INACTIVE == existingSubscription.getStatus)
				getEndDate(Instant.now, subscriptionPeriod)
			else
				getEndDate(existingSubscription.getEndDate, subscriptionPeriod)

		existingSubscription.setEndDate(newEndDate)
		existingSubscription.setStatus(Subscription.Status.ACTIVE)
		subscriptionDao.update(existingSubscription)
	}

	private def createAndPersistNewSubscription(userFromDb: User,
												periodical: Periodical,
												subscriptionPeriod: Int,
												subscriptionDao: SubscriptionDao): Unit = {
		val subscription = (new Subscription.Builder)
			.setUser(userFromDb)
			.setPeriodical(periodical)
			.setDeliveryAddress(userFromDb.getAddress)
			.setEndDate(getEndDate(Instant.now, subscriptionPeriod))
			.setStatus(Subscription.Status.ACTIVE)
			.build()

		subscriptionDao.createNew(subscription)
	}

	private def getEndDate(startInstant: Instant,
						   subscriptionPeriod: Int) = {
		val startDate = LocalDateTime.ofInstant(startInstant, ZoneId.systemDefault)
		startDate
			.plusMonths(subscriptionPeriod)
			.toInstant(ZoneOffset.UTC)
	}

	override def getFinStatistics(since: Instant, until: Instant): FinancialStatistics =
		withAbstractConnectionResource { conn =>
			val dao = factory.getInvoiceDao(conn)
			val totalInvoiceSum = dao.getCreatedInvoiceSumByCreationDate(since, until)
			val paidInvoiceSum = dao.getPaidInvoiceSumByPaymentDate(since, until)

			new FinancialStatistics(totalInvoiceSum, paidInvoiceSum)
		}
}
