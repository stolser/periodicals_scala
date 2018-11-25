package com.ostoliarov.webproject.controller.utils

import com.ostoliarov.webproject.controller.ApplicationResources._
import com.ostoliarov.webproject.controller.utils.HttpUtils.{firstIdFromUri, periodicalFromRequest, userIdFromSession}
import com.ostoliarov.webproject.model.entity.periodical.{PeriodicalCategory, PeriodicalStatus}
import com.ostoliarov.webproject.model.entity.user.User
import com.ostoliarov.webproject.{FunSuiteMockitoScalaBase, TestResources}
import javax.servlet.http.{HttpServletRequest, HttpSession}

/**
	* Created by Oleg Stoliarov on 11/4/18.
	*/
class HttpUtilsTest extends FunSuiteMockitoScalaBase {
	private val USER_ID = 2
	private var sessionMock: HttpSession = _
	private var requestMock: HttpServletRequest = _

	before {
		sessionMock = mock[HttpSession]
		when(sessionMock.getAttribute(CURRENT_USER_ATTR_NAME)) thenReturn User(id = USER_ID)

		requestMock = mock[HttpServletRequest]
		when(requestMock.getSession) thenReturn sessionMock
	}

	test("firstIdFromUri() Should return a correct id") {
		val uri = TestResources.USER_2_INVOICE_10_PAYMENT_URI
		val expected = 2
		val actual = firstIdFromUri(uri)

		assert(expected === actual)
	}

	test("firstIdFromUri() Should throw IllegalArgumentException") {
		assertThrows[IllegalArgumentException] {
			firstIdFromUri(TestResources.ALL_USERS_URI)
		}
	}

	test("userIdFromSession() Should return a correct id") {
		val expected = 2
		val actual = userIdFromSession(requestMock)

		assert(expected === actual)
	}

	test("periodicalFromRequest() Should return the correct periodical") {
		when(requestMock.getSession) thenReturn sessionMock
		when(requestMock.getParameter(ENTITY_ID_PARAM_NAME)) thenReturn "10"
		when(requestMock.getParameter(PERIODICAL_NAME_PARAM_NAME)) thenReturn "Test Name"
		when(requestMock.getParameter(PERIODICAL_CATEGORY_PARAM_NAME)) thenReturn "news"
		when(requestMock.getParameter(PERIODICAL_PUBLISHER_PARAM_NAME)) thenReturn "Test Publisher"
		when(requestMock.getParameter(PERIODICAL_DESCRIPTION_PARAM_NAME)) thenReturn "Test description"
		when(requestMock.getParameter(PERIODICAL_COST_PARAM_NAME)) thenReturn "99"
		when(requestMock.getParameter(PERIODICAL_STATUS_PARAM_NAME)) thenReturn "active"

		val periodical = periodicalFromRequest(requestMock)

		assert(periodical.getId == 10)
		assert(periodical.getName == "Test Name")
		assert(periodical.getCategory == PeriodicalCategory.NEWS)
		assert(periodical.getPublisher == "Test Publisher")
		assert(periodical.getDescription.get == "Test description")
		assert(periodical.getOneMonthCost == 99)
		assert(periodical.getStatus == PeriodicalStatus.ACTIVE)
	}
}
