package com.stolser.javatraining.webproject.controller.request.processor.periodical

import java.util.NoSuchElementException
import java.util.Objects.isNull

import com.stolser.javatraining.webproject.controller.ApplicationResources.{ONE_PERIODICAL_VIEW_NAME, PERIODICAL_ATTR_NAME}
import com.stolser.javatraining.webproject.controller.request.processor.RequestProcessor
import com.stolser.javatraining.webproject.controller.security.AccessDeniedException
import com.stolser.javatraining.webproject.controller.utils.{HttpUtils, HttpUtilsTrait}
import com.stolser.javatraining.webproject.model.entity.periodical.{Periodical, PeriodicalStatus}
import com.stolser.javatraining.webproject.model.entity.user.{User, UserRole}
import com.stolser.javatraining.webproject.service.PeriodicalService
import com.stolser.javatraining.webproject.service.impl.PeriodicalServiceImpl
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

/**
	* Created by Oleg Stoliarov on 10/11/18.
	* Processes a GET request to page with the information of the selected individual periodical.
	*/
object DisplayOnePeriodical extends RequestProcessor {
	private[periodical] var periodicalService: PeriodicalService = PeriodicalServiceImpl
	private[periodical] var httpUtils: HttpUtilsTrait = HttpUtils
	private val ACCESS_DENIED_TO = "Access denied to '%s'"

	override def process(request: HttpServletRequest, response: HttpServletResponse): String = {
		val currentUser = httpUtils.getCurrentUserFromFromDb(request)
		val periodicalId = httpUtils.getFirstIdFromUri(request.getRequestURI)
		val periodicalInDb = periodicalService.findOneById(periodicalId)

		checkPeriodicalExists(periodicalId, periodicalInDb)

		if (!hasUserEnoughPermissions(currentUser, periodicalInDb))
			throw new AccessDeniedException(String.format(ACCESS_DENIED_TO, request.getRequestURI))

		request.setAttribute(PERIODICAL_ATTR_NAME, periodicalInDb)

		FORWARD + ONE_PERIODICAL_VIEW_NAME
	}

	private def checkPeriodicalExists(periodicalId: Long, periodicalInDb: Periodical): Unit = {
		if (isNull(periodicalInDb))
			throw new NoSuchElementException(s"There is no periodical with id $periodicalId in the db.")
	}

	private def hasUserEnoughPermissions(currentUser: User, periodicalInDb: Periodical) =
		(PeriodicalStatus.ACTIVE == periodicalInDb.status
			|| currentUser.hasRole(UserRole.ADMIN))
}
