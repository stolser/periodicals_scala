package com.stolser.javatraining.webproject.dao.impl.mysql

import java.sql.{Connection, PreparedStatement, ResultSet, SQLException}

import com.stolser.javatraining.webproject._
import com.stolser.javatraining.webproject.controller.utils.{DaoUtils, DaoUtilsTrait}
import com.stolser.javatraining.webproject.dao.PeriodicalDao
import com.stolser.javatraining.webproject.dao.impl.mysql.MysqlPeriodicalDao._
import com.stolser.javatraining.webproject.model.entity.periodical.{Periodical, PeriodicalCategory, PeriodicalStatus}
import com.stolser.javatraining.webproject.model.entity.subscription.SubscriptionStatus

import scala.collection.mutable

/**
	* Created by Oleg Stoliarov on 10/14/18.
	*/

object MysqlPeriodicalDao {
	val DB_PERIODICALS_ID = "periodicals.id"
	val DB_PERIODICALS_NAME = "periodicals.name"
	val DB_PERIODICALS_CATEGORY = "periodicals.category"
	val DB_PERIODICALS_PUBLISHER = "periodicals.publisher"
	val DB_PERIODICALS_DESCRIPTION = "periodicals.description"
	val DB_PERIODICALS_ONE_MONTH_COST = "periodicals.one_month_cost"
	val DB_PERIODICALS_STATUS = "periodicals.status"
	private val INCORRECT_FIELD_NAME = "There is no case for such a fieldName." + "Fix it!"
	private val EXCEPTION_DURING_RETRIEVING_PERIODICAL = "Exception during retrieving a periodical with %s = %s. "
	private[mysql] val SELECT_ALL_PERIODICALS = "SELECT * FROM periodicals"
	private[mysql] val SELECT_ALL_BY_ID = "SELECT * FROM periodicals WHERE id = ?"
	private[mysql] val SELECT_ALL_BY_NAME = "SELECT * FROM periodicals WHERE name = ?"
	private[mysql] val SELECT_ALL_BY_STATUS = "SELECT * FROM periodicals WHERE status = ?"
	private[mysql] val INSERT_INTO_PERIODICALS_VALUES = "INSERT INTO periodicals " +
		"(name, category, publisher, description, one_month_cost, status) " +
		"VALUES (?, ?, ?, ?, ?, ?)"
	private[mysql] val DELETE_FROM_PERIODICALS_BY_STATUS = "DELETE FROM periodicals WHERE status = ?"
	private val EXCEPTION_DURING_RETRIEVING_ALL_PERIODICALS = "Exception during retrieving all periodicals."
	private val RETRIEVING_ALL_BY_STATUS = "Exception during retrieving periodicals with " + "status '%s'."
	private val EXCEPTION_DURING_INSERTING = "Exception during inserting %s into 'periodicals'."
	private val EXCEPTION_DURING_UPDATING = "Exception during updating %s."
	private val EXCEPTION_DURING_DELETING_DISCARDED_PERIODICALS = "Exception during deleting discarded periodicals."
	private val EXCEPTION_DURING_GETTING_NUMBER_OF_PERIODICALS =
		"Exception during getting number of periodicals with category = '%s' and status = '%s'."
}

case class MysqlPeriodicalDao private[mysql](conn: Connection) extends PeriodicalDao {
	private[mysql] val daoUtils: DaoUtilsTrait = DaoUtils

	override def findOneById(id: Long): Option[Periodical] =
		getPeriodicalFromDb(SELECT_ALL_BY_ID, id, DB_PERIODICALS_ID)

	override def findOneByName(name: String): Option[Periodical] =
		getPeriodicalFromDb(SELECT_ALL_BY_NAME, name, DB_PERIODICALS_NAME)

	private def getPeriodicalFromDb(sqlStatement: String,
																	fieldValue: Any,
																	fieldName: String): Option[Periodical] =
		tryAndCatchSqlException(EXCEPTION_DURING_RETRIEVING_PERIODICAL.format(fieldName, fieldValue)) { () =>
			withResources(conn.prepareStatement(sqlStatement)) {
				st: PreparedStatement => {
					setFieldValue(st, fieldName, fieldValue)

					withResources(st.executeQuery()) {
						rs: ResultSet =>
							if (rs.next())
								Some(daoUtils.periodicalFromResultSet(rs))
							else
								None
					}
				}
			}
		}

	@throws[SQLException]
	private def setFieldValue(st: PreparedStatement,
														fieldName: String,
														fieldValue: Any): Unit = fieldName match {
		case DB_PERIODICALS_ID =>
			st.setLong(1, fieldValue.asInstanceOf[Long])
		case DB_PERIODICALS_NAME =>
			st.setString(1, fieldValue.asInstanceOf[String])
		case _ =>
			throw new IllegalArgumentException(INCORRECT_FIELD_NAME)
	}

	override def findAll: List[Periodical] =
		tryAndCatchSqlException(exceptionMessage = EXCEPTION_DURING_RETRIEVING_ALL_PERIODICALS) { () =>
			withResources(conn.prepareStatement(SELECT_ALL_PERIODICALS)) {
				st: PreparedStatement => {

					withResources(st.executeQuery()) {
						rs: ResultSet => {
							val periodicals = mutable.Buffer[Periodical]()

							while (rs.next())
								periodicals += daoUtils.periodicalFromResultSet(rs)

							periodicals.toList
						}
					}
				}
			}
		}

	override def findAllByStatus(status: PeriodicalStatus.Value): List[Periodical] =
		tryAndCatchSqlException(exceptionMessage = RETRIEVING_ALL_BY_STATUS.format(status)) { () =>
			withResources(conn.prepareStatement(SELECT_ALL_BY_STATUS)) {
				st: PreparedStatement => {
					st.setString(1, status.toString.toLowerCase)

					withResources(st.executeQuery()) {
						rs: ResultSet => {
							val periodicals = mutable.Buffer[Periodical]()

							while (rs.next())
								periodicals += daoUtils.periodicalFromResultSet(rs)

							periodicals.toList
						}
					}
				}
			}
		}

	override def findNumberOfPeriodicalsWithCategoryAndStatus(category: PeriodicalCategory,
																														status: PeriodicalStatus.Value): Int = {
		val sqlStatement: String = "SELECT COUNT(id) FROM periodicals " +
			"WHERE category = ? AND status = ?"

		tryAndCatchSqlException(EXCEPTION_DURING_GETTING_NUMBER_OF_PERIODICALS.format(category, status)) { () =>
			withResources(conn.prepareStatement(sqlStatement)) {
				st: PreparedStatement => {
					st.setString(1, category.toString.toLowerCase)
					st.setString(2, status.toString.toLowerCase)

					withResources(st.executeQuery()) {
						rs: ResultSet => {
							rs.next
							rs.getInt(1)
						}
					}
				}
			}
		}
	}

	override def createNew(periodical: Periodical): Long =
		tryAndCatchSqlException(exceptionMessage = EXCEPTION_DURING_INSERTING.format(periodical)) { () =>
			withResources(conn.prepareStatement(INSERT_INTO_PERIODICALS_VALUES)) {
				st: PreparedStatement => {
					setStatementFromPeriodical(st, periodical)

					st.executeUpdate()
				}
			}
		}

	@throws[SQLException]
	private def setStatementFromPeriodical(st: PreparedStatement,
																				 periodical: Periodical): Unit = {
		st.setString(1, periodical.name)
		st.setString(2, periodical.category.toString.toLowerCase)
		st.setString(3, periodical.publisher)
		st.setString(4, periodical.description.orNull)
		st.setLong(5, periodical.oneMonthCost)
		st.setString(6, periodical.status.toString.toLowerCase)
	}

	override def update(periodical: Periodical): Int = {
		val sqlStatement: String = "UPDATE periodicals " +
			"SET name=?, category=?, publisher=?, description=?, one_month_cost=?, status=? " +
			"WHERE id=?"

		tryAndCatchSqlException(exceptionMessage = EXCEPTION_DURING_UPDATING.format(periodical)) { () =>
			withResources(conn.prepareStatement(sqlStatement)) {
				st: PreparedStatement => {
					setStatementFromPeriodical(st, periodical)
					st.setLong(7, periodical.id)

					st.executeUpdate()
				}
			}
		}
	}

	override def updateAndSetDiscarded(periodical: Periodical): Int = {
		val sqlStatement: String = "UPDATE periodicals AS p " +
			"SET name=?, category=?, publisher=?, description=?, one_month_cost=?, status=? " +
			"WHERE id=? AND 0 = (SELECT count(*) FROM subscriptions AS s " +
			"WHERE s.periodical_id = p.id AND s.status = ?)"

		tryAndCatchSqlException(exceptionMessage = EXCEPTION_DURING_UPDATING.format(periodical)) { () =>
			withResources(conn.prepareStatement(sqlStatement)) {
				st: PreparedStatement => {
					setStatementFromPeriodical(st, periodical)
					st.setLong(7, periodical.id)
					st.setString(8, SubscriptionStatus.ACTIVE.toString.toLowerCase)

					st.executeUpdate()
				}
			}
		}
	}

	override def deleteAllDiscarded(): Int =
		tryAndCatchSqlException(exceptionMessage = EXCEPTION_DURING_DELETING_DISCARDED_PERIODICALS) { () =>
			withResources(conn.prepareStatement(DELETE_FROM_PERIODICALS_BY_STATUS)) {
				st: PreparedStatement => {
					st.setString(1, PeriodicalStatus.DISCARDED.toString)

					st.executeUpdate
				}
			}
		}
}
