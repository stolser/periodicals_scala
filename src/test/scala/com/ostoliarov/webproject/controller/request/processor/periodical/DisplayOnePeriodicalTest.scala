package com.ostoliarov.webproject.controller.request.processor.periodical

import com.ostoliarov.webproject.FunSuiteMockitoScalaBase
import com.ostoliarov.webproject.controller.ApplicationResources._
import com.ostoliarov.webproject.controller.request.processor.periodical.DisplayOnePeriodical.FORWARD
import com.ostoliarov.webproject.controller.security.AccessDeniedException
import com.ostoliarov.webproject.controller.utils.HttpUtilsTrait
import com.ostoliarov.webproject.model.entity.periodical.{Periodical, PeriodicalStatus}
import com.ostoliarov.webproject.model.entity.user.{User, UserRole}
import com.ostoliarov.webproject.service.PeriodicalService
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.mockito.ArgumentMatchers._

/**
	* Created by Oleg Stoliarov on 11/6/18.
	*/
class DisplayOnePeriodicalTest extends FunSuiteMockitoScalaBase {
	private var requestMock: HttpServletRequest = _
	private var responseMock: HttpServletResponse = _
	private var periodicalServiceMock: PeriodicalService = _
	private var httpUtilsMock: HttpUtilsTrait = _

	before {
		requestMock = mock[HttpServletRequest]
		responseMock = mock[HttpServletResponse]
		periodicalServiceMock = mock[PeriodicalService]
		httpUtilsMock = mock[HttpUtilsTrait]
		DisplayOnePeriodical.httpUtils = httpUtilsMock
		DisplayOnePeriodical.periodicalService = periodicalServiceMock
	}

	test("process() Should throw NoSuchElementException if a periodical does not exist") {
		val periodicalId = 1
		when(httpUtilsMock.firstIdFromUri(anyString())) thenReturn periodicalId
		when(periodicalServiceMock.findOneById(periodicalId)) thenReturn None

		assertThrows[NoSuchElementException] {
			DisplayOnePeriodical.process(requestMock, responseMock)
		}
	}

	test("process() Should allow 'SUBSCRIBER' to see an 'ACTIVE' periodical") {
		val subscriber = Some(User(roles = Set(UserRole.SUBSCRIBER)))
		val periodicalId = 1
		val activePeriodical = Some(Periodical(status = PeriodicalStatus.ACTIVE))
		when(httpUtilsMock.currentUserFromFromDb(any[HttpServletRequest])) thenReturn subscriber
		when(httpUtilsMock.firstIdFromUri(anyString())) thenReturn periodicalId
		when(periodicalServiceMock.findOneById(periodicalId)) thenReturn activePeriodical

		val actualUri = DisplayOnePeriodical.process(requestMock, responseMock)

		verify(requestMock) setAttribute(PERIODICAL_ATTR_NAME, activePeriodical.get)

		assert((FORWARD + ONE_PERIODICAL_VIEW_NAME) === actualUri)
	}

	test("process() Should allow 'ADMIN' to see a 'DISCARDED' periodical") {
		val admin = Some(User(roles = Set(UserRole.ADMIN)))
		val periodicalId = 1
		val discardedPeriodical = Some(Periodical(status = PeriodicalStatus.DISCARDED))
		when(httpUtilsMock.currentUserFromFromDb(any[HttpServletRequest])) thenReturn admin
		when(httpUtilsMock.firstIdFromUri(anyString())) thenReturn periodicalId
		when(periodicalServiceMock.findOneById(periodicalId)) thenReturn discardedPeriodical

		val actualUri = DisplayOnePeriodical.process(requestMock, responseMock)

		verify(requestMock) setAttribute(PERIODICAL_ATTR_NAME, discardedPeriodical.get)

		assert((FORWARD + ONE_PERIODICAL_VIEW_NAME) === actualUri)
	}

	test("process() Should throw 'AccessDeniedException' if periodical is 'INACTIVE' and user is 'SUBSCRIBER'") {
		val subscriber = Some(User(roles = Set(UserRole.SUBSCRIBER)))
		val periodicalId = 1
		val inactivePeriodical = Some(Periodical(status = PeriodicalStatus.INACTIVE))
		when(httpUtilsMock.currentUserFromFromDb(any[HttpServletRequest])) thenReturn subscriber
		when(httpUtilsMock.firstIdFromUri(anyString())) thenReturn periodicalId
		when(periodicalServiceMock.findOneById(periodicalId)) thenReturn inactivePeriodical

		assertThrows[AccessDeniedException] {
			DisplayOnePeriodical.process(requestMock, responseMock)
		}
	}

	test("process() Should throw 'AccessDeniedException' if periodical is 'DISCARDED' and user is 'SUBSCRIBER'") {
		val subscriber = Some(User(roles = Set(UserRole.SUBSCRIBER)))
		val periodicalId = 1
		val discardedPeriodical = Some(Periodical(status = PeriodicalStatus.DISCARDED))
		when(httpUtilsMock.currentUserFromFromDb(any[HttpServletRequest])) thenReturn subscriber
		when(httpUtilsMock.firstIdFromUri(anyString())) thenReturn periodicalId
		when(periodicalServiceMock.findOneById(periodicalId)) thenReturn discardedPeriodical

		assertThrows[AccessDeniedException] {
			DisplayOnePeriodical.process(requestMock, responseMock)
		}
	}
}
