package com.stolser.javatraining.webproject.controller.request.processor.periodical

import com.stolser.javatraining.webproject.FunSuiteBase
import com.stolser.javatraining.webproject.controller.ApplicationResources._
import com.stolser.javatraining.webproject.controller.request.processor.periodical.DisplayOnePeriodical.FORWARD
import com.stolser.javatraining.webproject.controller.security.AccessDeniedException
import com.stolser.javatraining.webproject.controller.utils.HttpUtilsTrait
import com.stolser.javatraining.webproject.model.entity.periodical.{Periodical, PeriodicalStatus}
import com.stolser.javatraining.webproject.model.entity.user.{User, UserRole}
import com.stolser.javatraining.webproject.service.PeriodicalService
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.mockito.ArgumentMatchers._

/**
	* Created by Oleg Stoliarov on 11/6/18.
	*/
class DisplayOnePeriodicalTest extends FunSuiteBase {
	private var request: HttpServletRequest = _
	private var response: HttpServletResponse = _
	private var periodicalService: PeriodicalService = _
	private var httpUtils: HttpUtilsTrait = _

	before {
		request = mock[HttpServletRequest]
		response = mock[HttpServletResponse]
		periodicalService = mock[PeriodicalService]
		httpUtils = mock[HttpUtilsTrait]
		DisplayOnePeriodical.httpUtils = httpUtils
		DisplayOnePeriodical.periodicalService = periodicalService
	}

	test("process() Should throw NoSuchElementException if a periodical does not exist") {
		val periodicalId = 1
		when(httpUtils.firstIdFromUri(anyString())) thenReturn periodicalId
		when(periodicalService.findOneById(periodicalId)) thenReturn None

		assertThrows[NoSuchElementException] {
			DisplayOnePeriodical.process(request, response)
		}
	}

	test("process() Should allow 'SUBSCRIBER' to see an 'ACTIVE' periodical") {
		val subscriber = Some(User(roles = Set(UserRole.SUBSCRIBER)))
		val periodicalId = 1
		val activePeriodical = Some(Periodical(status = PeriodicalStatus.ACTIVE))
		when(httpUtils.currentUserFromFromDb(any[HttpServletRequest])) thenReturn subscriber
		when(httpUtils.firstIdFromUri(anyString())) thenReturn periodicalId
		when(periodicalService.findOneById(periodicalId)) thenReturn activePeriodical

		val actualUri = DisplayOnePeriodical.process(request, response)

		verify(request) setAttribute(PERIODICAL_ATTR_NAME, activePeriodical.get)

		assert((FORWARD + ONE_PERIODICAL_VIEW_NAME) === actualUri)
	}

	test("process() Should allow 'ADMIN' to see a 'DISCARDED' periodical") {
		val admin = Some(User(roles = Set(UserRole.ADMIN)))
		val periodicalId = 1
		val discardedPeriodical = Some(Periodical(status = PeriodicalStatus.DISCARDED))
		when(httpUtils.currentUserFromFromDb(any[HttpServletRequest])) thenReturn admin
		when(httpUtils.firstIdFromUri(anyString())) thenReturn periodicalId
		when(periodicalService.findOneById(periodicalId)) thenReturn discardedPeriodical

		val actualUri = DisplayOnePeriodical.process(request, response)

		verify(request) setAttribute(PERIODICAL_ATTR_NAME, discardedPeriodical.get)

		assert((FORWARD + ONE_PERIODICAL_VIEW_NAME) === actualUri)
	}

	test("process() Should throw 'AccessDeniedException' if periodical is 'INACTIVE' and user is 'SUBSCRIBER'") {
		val subscriber = Some(User(roles = Set(UserRole.SUBSCRIBER)))
		val periodicalId = 1
		val inactivePeriodical = Some(Periodical(status = PeriodicalStatus.INACTIVE))
		when(httpUtils.currentUserFromFromDb(any[HttpServletRequest])) thenReturn subscriber
		when(httpUtils.firstIdFromUri(anyString())) thenReturn periodicalId
		when(periodicalService.findOneById(periodicalId)) thenReturn inactivePeriodical

		assertThrows[AccessDeniedException] {
			DisplayOnePeriodical.process(request, response)
		}
	}

	test("process() Should throw 'AccessDeniedException' if periodical is 'DISCARDED' and user is 'SUBSCRIBER'") {
		val subscriber = Some(User(roles = Set(UserRole.SUBSCRIBER)))
		val periodicalId = 1
		val discardedPeriodical = Some(Periodical(status = PeriodicalStatus.DISCARDED))
		when(httpUtils.currentUserFromFromDb(any[HttpServletRequest])) thenReturn subscriber
		when(httpUtils.firstIdFromUri(anyString())) thenReturn periodicalId
		when(periodicalService.findOneById(periodicalId)) thenReturn discardedPeriodical

		assertThrows[AccessDeniedException] {
			DisplayOnePeriodical.process(request, response)
		}
	}
}
