package com.ostoliarov.webproject.controller.request.processor.periodical

import java.util.NoSuchElementException

import com.ostoliarov.webproject.controller.ApplicationResources.{ONE_PERIODICAL_VIEW_NAME, PERIODICAL_ATTR_NAME}
import com.ostoliarov.webproject.controller.request.processor.RequestProcessor
import com.ostoliarov.webproject.controller.security.AccessDeniedException
import com.ostoliarov.webproject.controller.security.AuthorizationFilter._
import com.ostoliarov.webproject.controller.utils.{HttpUtils, HttpUtilsTrait}
import com.ostoliarov.webproject.model.entity.periodical.{Periodical, PeriodicalStatus}
import com.ostoliarov.webproject.model.entity.user.{User, UserRole}
import com.ostoliarov.webproject.service.PeriodicalService
import com.ostoliarov.webproject.service.impl.mysql.PeriodicalServiceMysqlImpl
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

/**
	* Created by Oleg Stoliarov on 10/11/18.
	* Processes a GET request to page with the information of the selected individual periodical.
	*/
object DisplayOnePeriodical extends RequestProcessor {
	private[periodical] var periodicalService: PeriodicalService = PeriodicalServiceMysqlImpl
	private[periodical] var httpUtils: HttpUtilsTrait = HttpUtils

	override def process(request: HttpServletRequest,
											 response: HttpServletResponse): String = {
		val periodicalId = httpUtils.firstIdFromUri(request.getRequestURI)
		val periodicalInDb = periodicalService.findOneById(periodicalId)

		if (periodicalInDb.isEmpty)
			throw new NoSuchElementException(s"There is no periodical with id $periodicalId in the db.")

		val currentUser = httpUtils.currentUserFromFromDb(request)

		currentUser match {
			case Some(user) => checkPermissionsAndGetUri(request, periodicalInDb.get, user)
			case None =>
				throw new NoSuchElementException(s"There is no user in the db with id = ${httpUtils.userIdFromSession(request)}.")
		}
	}

	private def checkPermissionsAndGetUri(request: HttpServletRequest,
																				periodicalInDb: Periodical,
																				user: User): String = {
		if (hasUserEnoughPermissions(user, periodicalInDb)) {
			request.setAttribute(PERIODICAL_ATTR_NAME, periodicalInDb)

			FORWARD + ONE_PERIODICAL_VIEW_NAME
		} else
			throw AccessDeniedException(ACCESS_DENIED_FOR_USER.format(user.toString, request.getRequestURI))
	}

	private def hasUserEnoughPermissions(currentUser: User, periodicalInDb: Periodical) =
		(PeriodicalStatus.ACTIVE == periodicalInDb.status
			|| currentUser.hasRole(UserRole.ADMIN))
}
