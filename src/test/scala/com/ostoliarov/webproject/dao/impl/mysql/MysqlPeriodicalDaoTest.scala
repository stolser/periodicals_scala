package com.ostoliarov.webproject.dao.impl.mysql

import java.sql.{Connection, PreparedStatement, ResultSet}

import com.ostoliarov.webproject.FunSuiteWithMockitoScalaBase
import com.ostoliarov.webproject.controller.utils.DaoUtilsTrait
import com.ostoliarov.webproject.dao.impl.mysql.MysqlPeriodicalDao._
import com.ostoliarov.webproject.model.entity.periodical.PeriodicalStatus._
import com.ostoliarov.webproject.model.entity.periodical.{Periodical, PeriodicalCategory, PeriodicalStatus}

/**
	* Created by Oleg Stoliarov on 11/6/18.
	*/
class MysqlPeriodicalDaoTest extends FunSuiteWithMockitoScalaBase {
	var connMock: Connection = _
	var statementMock: PreparedStatement = _
	var resultSetMock: ResultSet = _
	var daoUtilsMock: DaoUtilsTrait = _
	var periodicalDao: MysqlPeriodicalDao = _

	before {
		connMock = mock[Connection]
		statementMock = mock[PreparedStatement]
		resultSetMock = mock[ResultSet]
		daoUtilsMock = mock[DaoUtilsTrait]
		periodicalDao = new MysqlPeriodicalDao(connMock) {
			override private[mysql] val daoUtils: DaoUtilsTrait = daoUtilsMock
		}
	}

	test("findOneById() Should return the correct periodical") {
		val periodicalId: Long = 1L
		val expectedPeriodical = Periodical(id = periodicalId)

		when(connMock.prepareStatement(SELECT_ALL_BY_ID)) thenReturn statementMock
		when(statementMock.executeQuery()) thenReturn resultSetMock
		when(resultSetMock.next()) thenReturn true
		when(daoUtilsMock.periodicalFromResultSet(resultSetMock)) thenReturn expectedPeriodical

		val actualPeriodical = periodicalDao.findOneById(periodicalId).get

		verify(statementMock).setLong(1, periodicalId)
		verify(statementMock).executeQuery
		verify(statementMock).close()

		assert(actualPeriodical === expectedPeriodical)
	}

	test("findAll() Should return all periodicals") {
		val expectedPeriodical1 = Periodical(id = 1)
		val expectedPeriodical2 = Periodical(id = 2)
		val expectedPeriodicals = List(expectedPeriodical1, expectedPeriodical2)

		when(connMock.prepareStatement(SELECT_ALL_PERIODICALS)) thenReturn statementMock
		when(statementMock.executeQuery()) thenReturn resultSetMock
		when(resultSetMock.next()) thenReturn(true, true, false)
		when(daoUtilsMock.periodicalFromResultSet(resultSetMock))
			.thenReturn(expectedPeriodical1, expectedPeriodical2)

		val actualPeriodicals = periodicalDao.findAll

		verify(statementMock).executeQuery
		verify(statementMock).close()

		assert(actualPeriodicals === expectedPeriodicals)
	}

	test("findAllByStatus() Should return all periodicals with the specified status") {
		val expectedPeriodical1 = Periodical(id = 1)
		val expectedPeriodical2 = Periodical(id = 2)
		val expectedPeriodical3 = Periodical(id = 3)
		val expectedPeriodicals = List(expectedPeriodical1, expectedPeriodical2, expectedPeriodical3)

		when(connMock.prepareStatement(SELECT_ALL_BY_STATUS)) thenReturn statementMock
		when(statementMock.executeQuery()) thenReturn resultSetMock
		when(resultSetMock.next()) thenReturn(true, true, true, false)
		when(daoUtilsMock.periodicalFromResultSet(resultSetMock))
			.thenReturn(expectedPeriodical1, expectedPeriodical2, expectedPeriodical3)

		val periodicalStatus = ACTIVE
		val actualPeriodicals = periodicalDao.findAllByStatus(periodicalStatus)

		verify(statementMock).setString(1, periodicalStatus.toString.toLowerCase)
		verify(statementMock).executeQuery
		verify(statementMock).close()

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

		when(connMock.prepareStatement(INSERT_INTO_PERIODICALS_VALUES)) thenReturn statementMock

		periodicalDao.createNew(newPeriodical)

		verify(statementMock).setString(1, newPeriodical.name)
		verify(statementMock).setString(2, newPeriodical.category.toString.toLowerCase)
		verify(statementMock).setString(3, newPeriodical.publisher)
		verify(statementMock).setString(4, newPeriodical.description.orNull)
		verify(statementMock).setLong(5, newPeriodical.oneMonthCost)
		verify(statementMock).setString(6, newPeriodical.status.toString.toLowerCase)

		verify(statementMock).executeUpdate()
		verify(statementMock).close()
	}

	test("deleteAllDiscarded() Should set up a statement with 'DISCARDED' status") {
		when(connMock.prepareStatement(DELETE_FROM_PERIODICALS_BY_STATUS)) thenReturn statementMock

		periodicalDao.deleteAllDiscarded()

		verify(statementMock).setString(1, PeriodicalStatus.DISCARDED.toString)
		verify(statementMock).executeUpdate()
		verify(statementMock).close()
	}
}
