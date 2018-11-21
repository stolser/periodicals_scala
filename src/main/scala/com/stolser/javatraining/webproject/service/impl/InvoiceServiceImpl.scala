package com.stolser.javatraining.webproject.service.impl

import java.time.{Instant, LocalDateTime, ZoneId, ZoneOffset}

import com.stolser.javatraining.webproject.dao.SubscriptionDao
import com.stolser.javatraining.webproject.model.entity.invoice.{Invoice, InvoiceStatus}
import com.stolser.javatraining.webproject.model.entity.periodical.Periodical
import com.stolser.javatraining.webproject.model.entity.statistics.FinancialStatistics
import com.stolser.javatraining.webproject.model.entity.subscription.{Subscription, SubscriptionStatus}
import com.stolser.javatraining.webproject.model.entity.user.User
import com.stolser.javatraining.webproject.service.InvoiceService
import com.stolser.javatraining.webproject.service.ServiceUtils.withConnection

/**
	* Created by Oleg Stoliarov on 11/21/18.
	*/
abstract class InvoiceServiceImpl extends InvoiceService {
	this: ServiceDependency =>

	override def findOneById(invoiceId: Long): Option[Invoice] =
		withConnection { conn =>
			daoFactory.invoiceDao(conn).findOneById(invoiceId)
		}

	override def findAllByUserId(userId: Long): List[Invoice] =
		withConnection { conn =>
			daoFactory.invoiceDao(conn).findAllByUserId(userId)
		}

	override def findAllByPeriodicalId(periodicalId: Long): List[Invoice] =
		withConnection { conn =>
			daoFactory.invoiceDao(conn).findAllByPeriodicalId(periodicalId)
		}

	override def createNew(invoice: Invoice): Unit = {
		require(invoice != null)

		withConnection { conn =>
			daoFactory.invoiceDao(conn).createNew(invoice)
		}
	}

	override def payInvoice(invoiceToPay: Invoice): Boolean = {
		require(invoiceToPay != null)

		withConnection { conn =>
			val subscriptionDao = daoFactory.subscriptionDao(conn)
			invoiceToPay.status = InvoiceStatus.PAID
			invoiceToPay.paymentDate = Some(Instant.now)

			conn.beginTransaction()

			daoFactory.invoiceDao(conn).update(invoiceToPay)

			val userInDb: User = daoFactory.userDao(conn).findOneById(invoiceToPay.user.id) match {
				case Some(user) => user
				case None => throw new RuntimeException(s"There is no user in the db associated with this invoice: $invoiceToPay")
			}

			val subscriptionInDb = subscriptionDao
				.findOneByUserIdAndPeriodicalId(userInDb.id, invoiceToPay.periodical.id)

			subscriptionInDb match {
				case Some(subscription) => updateExistingSubscription(subscription, invoiceToPay.subscriptionPeriod, subscriptionDao)
				case None => createAndPersistNewSubscription(userInDb, invoiceToPay.periodical, invoiceToPay.subscriptionPeriod, subscriptionDao)
			}

			conn.commitTransaction()

			true
		}
	}

	private def updateExistingSubscription(subscriptionInDb: Subscription,
																				 subscriptionPeriod: Int,
																				 subscriptionDao: SubscriptionDao): Unit = {
		val newEndDate =
			if (SubscriptionStatus.INACTIVE == subscriptionInDb.status)
				getEndDate(Some(Instant.now), subscriptionPeriod)
			else
				getEndDate(subscriptionInDb.endDate, subscriptionPeriod)

		subscriptionInDb.endDate = newEndDate
		subscriptionInDb.status = SubscriptionStatus.ACTIVE
		subscriptionDao.update(subscriptionInDb)
	}

	private def createAndPersistNewSubscription(userInDb: User,
																							periodical: Periodical,
																							subscriptionPeriod: Int,
																							subscriptionDao: SubscriptionDao): Unit =
		subscriptionDao.createNew(Subscription(
			user = userInDb,
			periodical = periodical,
			deliveryAddress = userInDb.address.getOrElse(""),
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
			case _ => None
		}

	override def finStatistics(since: Instant, until: Instant): FinancialStatistics =
		withConnection { conn =>
			val dao = daoFactory.invoiceDao(conn)
			val totalInvoiceSum = dao.createdInvoiceSumByCreationDate(since, until)
			val paidInvoiceSum = dao.paidInvoiceSumByPaymentDate(since, until)

			FinancialStatistics(totalInvoiceSum, paidInvoiceSum)
		}
}
