package com.ostoliarov.webproject.service.impl

import com.ostoliarov.webproject.WordSpecWithMockitoScalaBase
import com.ostoliarov.webproject.connection.AbstractConnection
import com.ostoliarov.webproject.connection.pool.ConnectionPool
import com.ostoliarov.webproject.dao.{DaoFactory, SubscriptionDao, UserDao}
import com.ostoliarov.webproject.model.entity.periodical.Periodical
import com.ostoliarov.webproject.model.entity.subscription.Subscription
import com.ostoliarov.webproject.model.entity.user.User
import com.ostoliarov.webproject.service.SubscriptionService

/**
	* Created by Oleg Stoliarov on 11/25/18.
	*/
class SubscriptionServiceImplTest extends WordSpecWithMockitoScalaBase {

	def fixture = new {
		val daoFactoryMock: DaoFactory = mock[DaoFactory]
		val connectionPoolMock: ConnectionPool = mock[ConnectionPool]
		val connMock: AbstractConnection = mock[AbstractConnection]
		val userDaoMock: UserDao = mock[UserDao]
		val subscriptionDaoMock: SubscriptionDao = mock[SubscriptionDao]

		val subscriptionServiceImpl: SubscriptionService =
			new SubscriptionServiceImpl with ServiceDependency {
				override implicit val connectionPool: ConnectionPool = connectionPoolMock
				override val daoFactory: DaoFactory = daoFactoryMock
			}

		when(connectionPoolMock.connection) thenReturn connMock
		when(daoFactoryMock.userDao(any[AbstractConnection])) thenReturn userDaoMock
		when(daoFactoryMock.subscriptionDao(any[AbstractConnection])) thenReturn subscriptionDaoMock
	}

	"findAllByUserId()" when {
		"called with non existing id" should {
			val f = fixture
			val nonExistingId = 11
			when(f.userDaoMock.findOneById(nonExistingId)) thenReturn None

			"return an empty list" in {
				f.subscriptionServiceImpl.findAllByUserId(nonExistingId) should have size 0
			}
		}

		"called with existing id of a user how has no subscriptions" should {
			val f = fixture
			val userId = 11
			val user = User(id = userId)
			when(f.userDaoMock.findOneById(userId)) thenReturn Some(user)
			when(f.subscriptionDaoMock.findAllByUser(user)) thenReturn List.empty

			"return an empty list" in {
				f.subscriptionServiceImpl.findAllByUserId(userId) should have size 0
			}
		}

		"called with existing id of a user how has 3 subscriptions" should {
			val f = fixture
			val userId = 11
			val user = User(id = userId)
			val expectedSubscriptions = List(
				Subscription(id = 1, user = user, periodical = Periodical(id = 10, name = "Test A")),
				Subscription(id = 2, user = user, periodical = Periodical(id = 11, name = "Test BB")),
				Subscription(id = 3, user = user, periodical = Periodical(id = 12, name = "Test CCC"))
			)
			when(f.userDaoMock.findOneById(userId)) thenReturn Some(user)
			when(f.subscriptionDaoMock.findAllByUser(user)) thenReturn expectedSubscriptions

			"return a list with the same 3 subscriptions" in {
				assert(f.subscriptionServiceImpl.findAllByUserId(userId) === expectedSubscriptions)
			}
		}
	}
}
