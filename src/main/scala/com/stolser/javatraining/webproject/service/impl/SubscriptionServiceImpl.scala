package com.stolser.javatraining.webproject.service.impl

import com.stolser.javatraining.webproject.connection.pool.{ConnectionPool, ConnectionPoolProvider}
import com.stolser.javatraining.webproject.dao.DaoFactory
import com.stolser.javatraining.webproject.model.entity.subscription.Subscription
import com.stolser.javatraining.webproject.service.ServiceUtils.withConnection
import com.stolser.javatraining.webproject.service.SubscriptionService

/**
	* Created by Oleg Stoliarov on 10/15/18.
	*/
object SubscriptionServiceImpl extends SubscriptionService {
	private lazy val factory = DaoFactory.mysqlDaoFactory
	private implicit lazy val connectionPool: ConnectionPool = ConnectionPoolProvider.getPool

	override def findAllByUserId(id: Long): List[Subscription] =
		withConnection { conn =>
			factory.userDao(conn).findOneById(id) match {
				case Some(user) => factory.subscriptionDao(conn).findAllByUser(user)
				case None => List.empty[Subscription]
			}
		}
}
