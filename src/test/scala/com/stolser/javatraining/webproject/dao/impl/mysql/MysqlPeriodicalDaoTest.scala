package com.stolser.javatraining.webproject.dao.impl.mysql

import java.sql.{Connection, PreparedStatement, ResultSet}

import com.stolser.javatraining.webproject.FunSuiteBase
import com.stolser.javatraining.webproject.controller.utils.DaoUtilsTrait
import com.stolser.javatraining.webproject.dao.impl.mysql.MysqlPeriodicalDao._
import com.stolser.javatraining.webproject.model.entity.periodical.PeriodicalStatus._
import com.stolser.javatraining.webproject.model.entity.periodical.{Periodical, PeriodicalCategory, PeriodicalStatus}

/**
	* Created by Oleg Stoliarov on 11/6/18.
	*/
class MysqlPeriodicalDaoTest extends FunSuiteBase {
	var conn: Connection = _
	var statement: PreparedStatement = _
	var resultSet: ResultSet = _
	var daoUtilsMock: DaoUtilsTrait = _
	var periodicalDao: MysqlPeriodicalDao = _

	before {
		conn = mock[Connection]
		statement = mock[PreparedStatement]
		resultSet = mock[ResultSet]
		daoUtilsMock = mock[DaoUtilsTrait]
		periodicalDao = new MysqlPeriodicalDao(conn) {
			override private[mysql] val daoUtils: DaoUtilsTrait = daoUtilsMock
		}
	}

	test("findOneById() Should return the correct periodical") {
		val periodicalId: Long = 1L
		val expectedPeriodical = Periodical(id = periodicalId)

		when(conn.prepareStatement(SELECT_ALL_BY_ID)) thenReturn statement
		when(statement.executeQuery()) thenReturn resultSet
		when(resultSet.next()) thenReturn true
		when(daoUtilsMock.periodicalFromResultSet(resultSet)) thenReturn expectedPeriodical

		val actualPeriodical = periodicalDao.findOneById(periodicalId).get

		verify(statement).setLong(1, periodicalId)
		verify(statement).executeQuery
		verify(statement).close()

		assert(actualPeriodical === expectedPeriodical)
	}

	test("findAll() Should return all periodicals") {
		val expectedPeriodical1 = Periodical(id = 1)
		val expectedPeriodical2 = Periodical(id = 2)
		val expectedPeriodicals = List(expectedPeriodical1, expectedPeriodical2)

		when(conn.prepareStatement(SELECT_ALL_PERIODICALS)) thenReturn statement
		when(statement.executeQuery()) thenReturn resultSet
		when(resultSet.next()) thenReturn(true, true, false)
		when(daoUtilsMock.periodicalFromResultSet(resultSet))
			.thenReturn(expectedPeriodical1, expectedPeriodical2)

		val actualPeriodicals = periodicalDao.findAll

		verify(statement).executeQuery
		verify(statement).close()

		assert(actualPeriodicals === expectedPeriodicals)
	}

	test("findAllByStatus() Should return all periodicals with the specified status") {
		val expectedPeriodical1 = Periodical(id = 1)
		val expectedPeriodical2 = Periodical(id = 2)
		val expectedPeriodical3 = Periodical(id = 3)
		val expectedPeriodicals = List(expectedPeriodical1, expectedPeriodical2, expectedPeriodical3)

		when(conn.prepareStatement(SELECT_ALL_BY_STATUS)) thenReturn statement
		when(statement.executeQuery()) thenReturn resultSet
		when(resultSet.next()) thenReturn(true, true, true, false)
		when(daoUtilsMock.periodicalFromResultSet(resultSet))
			.thenReturn(expectedPeriodical1, expectedPeriodical2, expectedPeriodical3)

		val periodicalStatus = ACTIVE
		val actualPeriodicals = periodicalDao.findAllByStatus(periodicalStatus)

		verify(statement).setString(1, periodicalStatus.toString.toLowerCase)
		verify(statement).executeQuery
		verify(statement).close()

		assert(actualPeriodicals === expectedPeriodicals)
	}

	test("createNew() Should set up a statement with correct data from a specified periodical") {
		val newPeriodical = Periodical(
			name = "Test Periodical",
			category = PeriodicalCategory.SPORTS,
			publisher = "Test Publisher",
			description = Some("Test description"),
			oneMonthCost = 7,
			status = PeriodicalStatus.INACTIVE
		)

		when(conn.prepareStatement(INSERT_INTO_PERIODICALS_VALUES)) thenReturn statement

		periodicalDao.createNew(newPeriodical)

		verify(statement).setString(1, newPeriodical.name)
		verify(statement).setString(2, newPeriodical.category.toString.toLowerCase)
		verify(statement).setString(3, newPeriodical.publisher)
		verify(statement).setString(4, newPeriodical.description.orNull)
		verify(statement).setLong(5, newPeriodical.oneMonthCost)
		verify(statement).setString(6, newPeriodical.status.toString.toLowerCase)

		verify(statement).executeUpdate()
		verify(statement).close()
	}

	test("deleteAllDiscarded() Should set up a statement with 'DISCARDED' status") {
		when(conn.prepareStatement(DELETE_FROM_PERIODICALS_BY_STATUS)) thenReturn statement

		periodicalDao.deleteAllDiscarded()

		verify(statement).setString(1, PeriodicalStatus.DISCARDED.toString)
		verify(statement).executeUpdate()
		verify(statement).close()
	}
}
