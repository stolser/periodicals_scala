package com.stolser.javatraining.webproject.dao.impl.mysql

import java.sql._
import java.time.Instant

import com.stolser.javatraining.webproject.dao.InvoiceDao
import com.stolser.javatraining.webproject.model.entity.invoice.{Invoice, InvoiceStatus}
import com.stolser.javatraining.webproject.model.entity.periodical.Periodical
import com.stolser.javatraining.webproject.model.entity.user.User
import com.stolser.javatraining.webproject.utils.TryCatchUtils._

import scala.collection.mutable

/**
	* Created by Oleg Stoliarov on 10/14/18.
	*/

object MysqlInvoiceDao {
	private val DB_INVOICES_ID = "invoices.id"
	private val DB_INVOICES_USER_ID = "invoices.user_id"
	private val DB_INVOICES_PERIODICAL_ID = "invoices.periodical_id"
	private val DB_INVOICES_PERIOD = "invoices.period"
	private val DB_INVOICES_TOTAL_SUM = "invoices.total_sum"
	private val DB_INVOICES_STATUS = "invoices.status"
	private val DB_INVOICES_CREATION_DATE = "invoices.creation_date"
	private val DB_INVOICES_PAYMENT_DATE = "invoices.payment_date"
	private val EXCEPTION_DURING_EXECUTION_STATEMENT_FOR_INVOICE_ID = "Exception during execution statement '%s' for invoiceId = %d."
	private val EXCEPTION_DURING_EXECUTION_STATEMENT_FOR_USER_ID = "Exception during execution statement '%s' for userId = %d."
	private val EXCEPTION_DURING_EXECUTION_STATEMENT_FOR_INVOICE = "Exception during execution statement '%s' for invoice = %s."
	private val EXCEPTION_DURING_GETTING_INVOICE_SUM = "Exception during execution statement '%s' for since = %s " + "and until = '%s'."
	private val EXCEPTION_DURING_EXECUTION_FOR_PERIODICAL_ID = "Exception during execution statement '%s' for periodicalId = %d."
}

class MysqlInvoiceDao(conn: Connection) extends InvoiceDao {

	import MysqlInvoiceDao._

	override def findOneById(invoiceId: Long): Invoice = {
		val sqlStatement: String = "SELECT * FROM invoices WHERE id = ?"
		val exceptionMessage = EXCEPTION_DURING_EXECUTION_STATEMENT_FOR_INVOICE_ID.format(sqlStatement, invoiceId)

		tryAndCatchSqlException(exceptionMessage) { () =>
			withResources(conn.prepareStatement(sqlStatement)) {
				st: PreparedStatement => {
					st.setLong(1, invoiceId)

					withResources(st.executeQuery()) {
						rs: ResultSet => {
							if (rs.next())
								getInvoiceFromRs(rs)
							else
								null
						}
					}

				}
			}
		}
	}

	override def findAllByUserId(userId: Long): List[Invoice] = {
		val sqlStatement: String = "SELECT * FROM invoices " +
			"JOIN users ON (invoices.user_id = users.id) " +
			"WHERE users.id = ?"
		val exceptionMessage = EXCEPTION_DURING_EXECUTION_STATEMENT_FOR_USER_ID.format(sqlStatement, userId)

		tryAndCatchSqlException(exceptionMessage) { () =>
			executeAndGetInvoicesFromRs(sqlStatement, userId)
		}
	}

	override def findAllByPeriodicalId(periodicalId: Long): List[Invoice] = {
		val sqlStatement: String = "SELECT * FROM invoices " +
			"JOIN periodicals ON (invoices.periodical_id = periodicals.id) " +
			"WHERE periodicals.id = ?"
		val exceptionMessage = EXCEPTION_DURING_EXECUTION_FOR_PERIODICAL_ID.format(sqlStatement, periodicalId)

		tryAndCatchSqlException(exceptionMessage) { () =>
			executeAndGetInvoicesFromRs(sqlStatement, periodicalId)
		}
	}

	@throws[SQLException]
	private def executeAndGetInvoicesFromRs(sqlStatement: String,
																					periodicalId: Long): List[Invoice] =
		withResources(conn.prepareStatement(sqlStatement)) {
			st: PreparedStatement => {
				st.setLong(1, periodicalId)

				withResources(st.executeQuery()) {
					rs: ResultSet => {
						val invoices = mutable.Buffer[Invoice]()
						while (rs.next)
							invoices += getInvoiceFromRs(rs)

						invoices.toList
					}
				}
			}
		}

	override def createdInvoiceSumByCreationDate(since: Instant,
																							 until: Instant): Long = {
		val sqlStatement = "SELECT SUM(total_sum) FROM invoices " +
			"WHERE creation_date >= ? AND creation_date <= ?"
		val exceptionMessage = EXCEPTION_DURING_GETTING_INVOICE_SUM.format(sqlStatement, since, until)

		tryAndCatchSqlException(exceptionMessage) { () =>
			withResources(conn.prepareStatement(sqlStatement)) {
				st: PreparedStatement => {
					st.setTimestamp(1, new Timestamp(since.toEpochMilli))
					st.setTimestamp(2, new Timestamp(until.toEpochMilli))

					withResources(st.executeQuery()) {
						rs: ResultSet => {
							rs.next()
							rs.getLong(1)
						}
					}
				}
			}
		}
	}

	override def paidInvoiceSumByPaymentDate(since: Instant, until: Instant): Long = {
		val sqlStatement: String = "SELECT SUM(total_sum) FROM invoices " +
			"WHERE payment_date >= ? AND payment_date <= ? AND status = ?"
		val exceptionMessage = EXCEPTION_DURING_GETTING_INVOICE_SUM.format(sqlStatement, since, until)

		tryAndCatchSqlException(exceptionMessage) { () =>
			withResources(conn.prepareStatement(sqlStatement)) {
				st: PreparedStatement => {
					st.setTimestamp(1, new Timestamp(since.toEpochMilli))
					st.setTimestamp(2, new Timestamp(until.toEpochMilli))
					st.setString(3, InvoiceStatus.PAID.toString.toLowerCase())

					withResources(st.executeQuery()) {
						rs: ResultSet => {
							rs.next()
							rs.getLong(1)
						}
					}
				}
			}
		}
	}


	override def createNew(invoice: Invoice): Long = {
		val sqlStatement: String = "INSERT INTO invoices " +
			"(user_id, periodical_id, period, total_sum, creation_date, payment_date, status) " +
			"VALUES (?, ?, ?, ?, ?, ?, ?)"
		val exceptionMessage = EXCEPTION_DURING_EXECUTION_STATEMENT_FOR_INVOICE.format(sqlStatement, invoice)

		tryAndCatchSqlException(exceptionMessage) { () =>
			withResources(conn.prepareStatement(sqlStatement)) {
				st: PreparedStatement => {
					setCreateUpdateStatementFromInvoice(st, invoice)
					st.executeUpdate()
				}
			}
		}
	}

	override def update(invoice: Invoice): Int = {
		val sqlStatement: String = "UPDATE invoices " +
			"SET user_id=?, periodical_id=?, period=?, total_sum=?, creation_date=?, " +
			"payment_date=?, status=? WHERE id=?"
		val exceptionMessage = EXCEPTION_DURING_EXECUTION_STATEMENT_FOR_INVOICE.format(sqlStatement, invoice)

		tryAndCatchSqlException(exceptionMessage) { () =>
			withResources(conn.prepareStatement(sqlStatement)) {
				st: PreparedStatement => {
					setCreateUpdateStatementFromInvoice(st, invoice)
					st.setLong(8, invoice.id)

					st.executeUpdate()
				}
			}
		}
	}

	@throws[SQLException]
	private def getInvoiceFromRs(rs: ResultSet): Invoice = {
		val user = User(
			id = rs.getLong(DB_INVOICES_USER_ID)
		)
		val periodical = Periodical(
			id = rs.getLong(DB_INVOICES_PERIODICAL_ID)
		)

		Invoice(
			id = rs.getLong(DB_INVOICES_ID),
			user = user,
			periodical = periodical,
			subscriptionPeriod = rs.getInt(DB_INVOICES_PERIOD),
			totalSum = rs.getLong(DB_INVOICES_TOTAL_SUM),
			creationDate = getCreationDateFromResults(rs),
			paymentDate = getPaymentDateFromResults(rs),
			status = InvoiceStatus.withName(rs.getString(DB_INVOICES_STATUS).toUpperCase)
		)
	}

	@throws[SQLException]
	private def getCreationDateFromResults(rs: ResultSet): Option[Instant] =
		Option(Instant.ofEpochMilli(rs.getTimestamp(DB_INVOICES_CREATION_DATE).getTime))

	@throws[SQLException]
	private def getPaymentDateFromResults(rs: ResultSet): Option[Instant] =
		rs.getTimestamp(DB_INVOICES_PAYMENT_DATE) match {
			case timestamp: Timestamp => Some(Instant.ofEpochMilli(timestamp.getTime))
			case _ => None
		}

	@throws[SQLException]
	private def setCreateUpdateStatementFromInvoice(st: PreparedStatement,
																									invoice: Invoice): Unit = {
		st.setLong(1, invoice.user.id)
		st.setLong(2, invoice.periodical.id)
		st.setInt(3, invoice.subscriptionPeriod)
		st.setDouble(4, invoice.totalSum)
		st.setTimestamp(5, getCreationDate(invoice))
		st.setTimestamp(6, getPaymentDate(invoice))
		st.setString(7, invoice.status.toString.toLowerCase)
	}

	private def getCreationDate(invoice: Invoice): Timestamp =
		invoice.creationDate match {
			case Some(creationDate) => new Timestamp(creationDate.toEpochMilli)
			case None => null
		}

	private def getPaymentDate(invoice: Invoice): Timestamp =
		invoice.paymentDate match {
			case Some(paymentDate) => new Timestamp(paymentDate.toEpochMilli)
			case None => null
		}

	override def findAll: List[Invoice] =
		throw new UnsupportedOperationException
}
