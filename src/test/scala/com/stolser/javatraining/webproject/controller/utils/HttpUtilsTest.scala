package com.stolser.javatraining.webproject.controller.utils

import com.stolser.javatraining.webproject.controller.ApplicationResources._
import com.stolser.javatraining.webproject.model.entity.periodical.{PeriodicalCategory, PeriodicalStatus}
import com.stolser.javatraining.webproject.model.entity.user.User
import com.stolser.javatraining.webproject.{FunSuiteBase, TestResources}
import javax.servlet.http.{HttpServletRequest, HttpSession}

/**
	* Created by Oleg Stoliarov on 11/4/18.
	*/
class HttpUtilsTest extends FunSuiteBase {
	private val USER_ID = 2
	private var session: HttpSession = _
	private var request: HttpServletRequest = _

	before {
		session = mock[HttpSession]
		when(session.getAttribute(CURRENT_USER_ATTR_NAME)) thenReturn User(id = USER_ID)

		request = mock[HttpServletRequest]
		when(request.getSession) thenReturn session
	}

	test("getFirstIdFromUri() Should return a correct id") {
		val uri = TestResources.USER_2_INVOICE_10_PAYMENT_URI
		val expected = 2
		val actual = HttpUtils.getFirstIdFromUri(uri)

		assert(expected === actual)
	}

	test("getFirstIdFromUri() Should throw IllegalArgumentException") {
		assertThrows[IllegalArgumentException] {
			HttpUtils.getFirstIdFromUri(TestResources.ALL_USERS_URI)
		}
	}

	test("getUserIdFromSession() Should return a correct id") {
		val expected = 2
		val actual = HttpUtils.getUserIdFromSession(request)

		assert(expected === actual)
	}

	test("getPeriodicalFromRequest() Should return the correct periodical") {
		when(request.getSession) thenReturn session
		when(request.getParameter(ENTITY_ID_PARAM_NAME)) thenReturn "10"
		when(request.getParameter(PERIODICAL_NAME_PARAM_NAME)) thenReturn "Test Name"
		when(request.getParameter(PERIODICAL_CATEGORY_PARAM_NAME)) thenReturn "news"
		when(request.getParameter(PERIODICAL_PUBLISHER_PARAM_NAME)) thenReturn "Test Publisher"
		when(request.getParameter(PERIODICAL_DESCRIPTION_PARAM_NAME)) thenReturn "Test description"
		when(request.getParameter(PERIODICAL_COST_PARAM_NAME)) thenReturn "99"
		when(request.getParameter(PERIODICAL_STATUS_PARAM_NAME)) thenReturn "active"

		val periodical = HttpUtils.getPeriodicalFromRequest(request)

		assert(periodical.getId == 10)
		assert(periodical.getName == "Test Name")
		assert(periodical.getCategory == PeriodicalCategory.NEWS)
		assert(periodical.getPublisher == "Test Publisher")
		assert(periodical.getDescription.get == "Test description")
		assert(periodical.getOneMonthCost == 99)
		assert(periodical.getStatus == PeriodicalStatus.ACTIVE)
	}
}
