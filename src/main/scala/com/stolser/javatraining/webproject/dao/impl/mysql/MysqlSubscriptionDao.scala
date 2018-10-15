package com.stolser.javatraining.webproject.dao.impl.mysql

import java.sql._
import java.util
import java.util.{ArrayList, List}

import com.stolser.javatraining.webproject.controller.utils.DaoUtils
import com.stolser.javatraining.webproject.dao.DaoUtils.tryAndCatchSqlException
import com.stolser.javatraining.webproject.dao.SubscriptionDao
import com.stolser.javatraining.webproject.utils.TryWithResources.withResources
import com.stolser.javatraining.webproject.model.entity.periodical.Periodical
import com.stolser.javatraining.webproject.model.entity.subscription.Subscription
import com.stolser.javatraining.webproject.model.entity.user.User

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

class MysqlSubscriptionDao(conn: Connection) extends SubscriptionDao {

	import MysqlSubscriptionDao._

	override def findOneByUserIdAndPeriodicalId(userId: Long, periodicalId: Long): Subscription = {
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
								newSubscriptionFromRs(rs)
							else
								null
					}
				}
			}
		}
	}

	override def findAllByPeriodicalIdAndStatus(periodicalId: Long,
												status: Subscription.Status): util.List[Subscription] = {
		val sqlStatement = "SELECT * FROM subscriptions " +
			"JOIN periodicals ON (subscriptions.periodical_id = periodicals.id) " +
			"WHERE periodicals.id = ? AND subscriptions.status = ?"

		tryAndCatchSqlException(exceptionMessage = EXCEPTION_MSG_FINDING_ALL_BY_ID.format(periodicalId, status)) { () =>
			withResources(conn.prepareStatement(sqlStatement)) {
				st: PreparedStatement => {
					st.setLong(1, periodicalId)
					st.setString(2, status.name.toLowerCase)

					withResources(st.executeQuery()) {
						rs: ResultSet =>
							val subscriptions = new util.ArrayList[Subscription]

							while (rs.next)
								subscriptions.add(newSubscriptionFromRs(rs))

							subscriptions
					}
				}
			}
		}
	}

	@throws[SQLException]
	private def newSubscriptionFromRs(rs: ResultSet) = {
		val user = (new User.Builder)
			.setId(rs.getLong(DB_SUBSCRIPTIONS_USER_ID))
			.build()

		val periodical = (new Periodical.Builder)
			.setId(rs.getLong(DB_SUBSCRIPTIONS_PERIODICAL_ID))
			.build()

		(new Subscription.Builder)
			.setId(rs.getLong(DB_SUBSCRIPTIONS_ID))
			.setUser(user)
			.setPeriodical(periodical)
			.setDeliveryAddress(rs.getString(DB_SUBSCRIPTIONS_DELIVERY_ADDRESS))
			.setEndDate(rs.getTimestamp(DB_SUBSCRIPTIONS_END_DATE).toInstant)
			.setStatus(Subscription.Status.valueOf(rs.getString(DB_SUBSCRIPTIONS_STATUS).toUpperCase))
			.build
	}

	override def findAllByUser(user: User): util.List[Subscription] = {
		val sqlStatement = "SELECT * FROM users " +
			"JOIN subscriptions ON (users.id = subscriptions.user_id) " +
			"JOIN periodicals ON (subscriptions.periodical_id = periodicals.id) " +
			"WHERE users.id = ?"

		tryAndCatchSqlException(exceptionMessage = EXCEPTION_MSG_RETRIEVING_SUBSCRIPTIONS_FOR_USER.format(user)) { () =>
			withResources(conn.prepareStatement(sqlStatement)) {
				st: PreparedStatement => {
					st.setLong(1, user.getId)

					withResources(st.executeQuery()) {
						rs: ResultSet =>
							val subscriptions = new util.ArrayList[Subscription]
							while (rs.next) {
								val subscription = (new Subscription.Builder)
									.setId(rs.getLong(DB_SUBSCRIPTIONS_ID))
									.setUser(user)
									.setPeriodical(DaoUtils.getPeriodicalFromResultSet(rs))
									.setDeliveryAddress(rs.getString(DB_SUBSCRIPTIONS_DELIVERY_ADDRESS))
									.setEndDate(rs.getTimestamp(DB_SUBSCRIPTIONS_END_DATE).toInstant)
									.setStatus(Subscription.Status.valueOf(rs.getString(DB_SUBSCRIPTIONS_STATUS).toUpperCase))
									.build()
								subscriptions.add(subscription)
							}

							subscriptions
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
		st.setLong(1, subscription.getUser.getId)
		st.setLong(2, subscription.getPeriodical.getId)
		st.setString(3, subscription.getDeliveryAddress)
		st.setTimestamp(4, new Timestamp(subscription.getEndDate.toEpochMilli))
		st.setString(5, subscription.getStatus.name.toLowerCase)
	}

	override def update(subscription: Subscription): Int = {
		val sqlStatement = "UPDATE subscriptions " +
			"SET user_id=?, periodical_id=?, delivery_address=?, end_date=?, status=? " +
			"WHERE id=?"

		tryAndCatchSqlException(exceptionMessage = EXCEPTION_MSG_UPDATING.format(subscription)) { () =>
			withResources(conn.prepareStatement(sqlStatement)) {
				st: PreparedStatement => {
					setSubscriptionForInsertUpdateStatement(st, subscription)
					st.setLong(6, subscription.getId)

					st.executeUpdate
				}
			}
		}
	}

	override def findOneById(id: Long) = throw new UnsupportedOperationException

	override def findAll = throw new UnsupportedOperationException
}
