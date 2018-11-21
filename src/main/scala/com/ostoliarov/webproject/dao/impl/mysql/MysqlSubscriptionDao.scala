package com.ostoliarov.webproject.dao.impl.mysql

import java.sql._

import com.ostoliarov.webproject._
import com.ostoliarov.webproject.controller.utils.DaoUtils
import com.ostoliarov.webproject.dao.SubscriptionDao
import com.ostoliarov.webproject.dao.impl.mysql.MysqlSubscriptionDao._
import com.ostoliarov.webproject.model.entity.periodical.Periodical
import com.ostoliarov.webproject.model.entity.subscription.{Subscription, SubscriptionStatus}
import com.ostoliarov.webproject.model.entity.user.User

import scala.collection.mutable

/**
	* Created by Oleg Stoliarov on 10/14/18.
	*/

object MysqlSubscriptionDao {
	private val DB_SUBSCRIPTIONS_ID = "subscriptions.id"
	private val DB_SUBSCRIPTIONS_USER_ID = "subscriptions.user_id"
	private val DB_SUBSCRIPTIONS_PERIODICAL_ID = "subscriptions.periodical_id"
	private val DB_SUBSCRIPTIONS_DELIVERY_ADDRESS = "subscriptions.delivery_address"
	private val DB_SUBSCRIPTIONS_END_DATE = "subscriptions.end_date"
	private val DB_SUBSCRIPTIONS_STATUS = "subscriptions.status"
	private val EXCEPTION_MSG_FINDING_ALL_PERIODICALS_BY_USER_ID = "Exception during finding all periodicals for userId = %d, " + "periodicalId = %d"
	private val EXCEPTION_MSG_FINDING_ALL_BY_ID = "Exception during finding all periodicals for periodicalId = %d, " + "status = %s"
	private val EXCEPTION_MSG_RETRIEVING_SUBSCRIPTIONS_FOR_USER = "Exception during retrieving subscriptions for a user: %s."
	private val EXCEPTION_MSG_CREATING_SUBSCRIPTION = "Exception during creating a subscription %s."
	private val EXCEPTION_MSG_UPDATING = "Exception during updating %s."
}

class MysqlSubscriptionDao private[mysql](conn: Connection) extends SubscriptionDao {

	override def findOneByUserIdAndPeriodicalId(userId: Long, periodicalId: Long): Option[Subscription] = {
		val sqlStatement = "SELECT * FROM subscriptions " +
			"WHERE user_id = ? AND periodical_id = ?"
		val exceptionMessage = EXCEPTION_MSG_FINDING_ALL_PERIODICALS_BY_USER_ID.format(userId, periodicalId)

		tryAndCatchSqlException(exceptionMessage) { () =>
			withResources(conn.prepareStatement(sqlStatement)) {
				st: PreparedStatement => {
					st.setLong(1, userId)
					st.setLong(2, periodicalId)

					withResources(st.executeQuery()) {
						rs: ResultSet =>
							if (rs.next)
								Some(newSubscriptionFromRs(rs))
							else None
					}
				}
			}
		}
	}

	override def findAllByPeriodicalIdAndStatus(periodicalId: Long,
																							status: SubscriptionStatus.Value): List[Subscription] = {
		val sqlStatement = "SELECT * FROM subscriptions " +
			"JOIN periodicals ON (subscriptions.periodical_id = periodicals.id) " +
			"WHERE periodicals.id = ? AND subscriptions.status = ?"

		tryAndCatchSqlException(exceptionMessage = EXCEPTION_MSG_FINDING_ALL_BY_ID.format(periodicalId, status)) { () =>
			withResources(conn.prepareStatement(sqlStatement)) {
				st: PreparedStatement => {
					st.setLong(1, periodicalId)
					st.setString(2, status.toString.toLowerCase)

					withResources(st.executeQuery()) {
						rs: ResultSet =>
							val subscriptions = mutable.Buffer[Subscription]()

							while (rs.next)
								subscriptions += newSubscriptionFromRs(rs)

							subscriptions.toList
					}
				}
			}
		}
	}

	@throws[SQLException]
	private def newSubscriptionFromRs(rs: ResultSet) = {
		val user = User(
			id = rs.getLong(DB_SUBSCRIPTIONS_USER_ID)
		)

		val periodical = Periodical(
			id = rs.getLong(DB_SUBSCRIPTIONS_PERIODICAL_ID)
		)

		Subscription(
			id = rs.getLong(DB_SUBSCRIPTIONS_ID),
			user = user,
			periodical = periodical,
			deliveryAddress = rs.getString(DB_SUBSCRIPTIONS_DELIVERY_ADDRESS),
			endDate = Option(rs.getTimestamp(DB_SUBSCRIPTIONS_END_DATE).toInstant),
			status = SubscriptionStatus.withName(rs.getString(DB_SUBSCRIPTIONS_STATUS).toUpperCase)
		)
	}

	override def findAllByUser(user: User): List[Subscription] = {
		val sqlStatement = "SELECT * FROM users " +
			"JOIN subscriptions ON (users.id = subscriptions.user_id) " +
			"JOIN periodicals ON (subscriptions.periodical_id = periodicals.id) " +
			"WHERE users.id = ?"

		tryAndCatchSqlException(exceptionMessage = EXCEPTION_MSG_RETRIEVING_SUBSCRIPTIONS_FOR_USER.format(user)) { () =>
			withResources(conn.prepareStatement(sqlStatement)) {
				st: PreparedStatement => {
					st.setLong(1, user.id)

					withResources(st.executeQuery()) {
						rs: ResultSet =>
							val subscriptions = mutable.Buffer[Subscription]()
							while (rs.next)
								subscriptions += Subscription(
									id = rs.getLong(DB_SUBSCRIPTIONS_ID),
									user = user,
									periodical = DaoUtils.periodicalFromResultSet(rs),
									deliveryAddress = rs.getString(DB_SUBSCRIPTIONS_DELIVERY_ADDRESS),
									endDate = Option(rs.getTimestamp(DB_SUBSCRIPTIONS_END_DATE).toInstant),
									status = SubscriptionStatus.withName(rs.getString(DB_SUBSCRIPTIONS_STATUS).toUpperCase)
								)

							subscriptions.toList
					}
				}
			}
		}
	}

	override def createNew(subscription: Subscription): Long = {
		val sqlStatement = "INSERT INTO subscriptions " +
			"(user_id, periodical_id, delivery_address, end_date, status) " +
			"VALUES (?, ?, ?, ?, ?)"

		tryAndCatchSqlException(exceptionMessage = EXCEPTION_MSG_CREATING_SUBSCRIPTION.format(subscription)) { () =>
			withResources(conn.prepareStatement(sqlStatement)) {
				st: PreparedStatement => {
					setSubscriptionForInsertUpdateStatement(st, subscription)

					st.executeUpdate
				}
			}
		}
	}

	@throws[SQLException]
	private def setSubscriptionForInsertUpdateStatement(st: PreparedStatement,
																											subscription: Subscription): Unit = {
		st.setLong(1, subscription.user.id)
		st.setLong(2, subscription.periodical.id)
		st.setString(3, subscription.deliveryAddress)
		st.setTimestamp(4, getEndDate(subscription))
		st.setString(5, subscription.status.toString.toLowerCase)
	}

	private def getEndDate(subscription: Subscription) =
		subscription.endDate match {
			case Some(date) => new Timestamp(date.toEpochMilli)
			case None => null
		}

	override def update(subscription: Subscription): Int = {
		val sqlStatement = "UPDATE subscriptions " +
			"SET user_id=?, periodical_id=?, delivery_address=?, end_date=?, status=? " +
			"WHERE id=?"

		tryAndCatchSqlException(exceptionMessage = EXCEPTION_MSG_UPDATING.format(subscription)) { () =>
			withResources(conn.prepareStatement(sqlStatement)) {
				st: PreparedStatement => {
					setSubscriptionForInsertUpdateStatement(st, subscription)
					st.setLong(6, subscription.id)

					st.executeUpdate
				}
			}
		}
	}

	override def findOneById(id: Long): Option[Subscription] = throw new UnsupportedOperationException

	override def findAll: List[Subscription] = throw new UnsupportedOperationException
}
