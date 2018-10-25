package com.stolser.javatraining.webproject.service.impl

import java.util

import com.stolser.javatraining.webproject.connection.pool.{ConnectionPool, ConnectionPoolProvider}
import com.stolser.javatraining.webproject.dao.DaoFactory
import com.stolser.javatraining.webproject.model.entity.subscription.Subscription
import com.stolser.javatraining.webproject.service.ServiceUtils.withConnection
import com.stolser.javatraining.webproject.service.SubscriptionService

/**
  * Created by Oleg Stoliarov on 10/15/18.
  */
object SubscriptionServiceImpl extends SubscriptionService {
	private lazy val factory = DaoFactory.getMysqlDaoFactory
	private implicit lazy val connectionPool: ConnectionPool = ConnectionPoolProvider.getPool

	override def findAllByUserId(id: Long): util.List[Subscription] =
		withConnection { conn =>
			factory.getSubscriptionDao(conn)
				.findAllByUser(factory.getUserDao(conn).findOneById(id))
		}
}
