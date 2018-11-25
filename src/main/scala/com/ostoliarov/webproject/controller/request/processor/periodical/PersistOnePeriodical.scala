package com.ostoliarov.webproject.controller.request.processor.periodical

import com.ostoliarov.webproject.controller.ApplicationResources._
import com.ostoliarov.webproject.controller.form.validator.ValidatorFactory
import com.ostoliarov.webproject.controller.message.{FrontMessageFactory, FrontendMessage}
import com.ostoliarov.webproject.controller.request.processor.DispatchType.REDIRECT
import com.ostoliarov.webproject.controller.request.processor.{AbstractViewName, RequestProcessor, ResourceRequest}
import com.ostoliarov.webproject.controller.utils.HttpUtils._
import com.ostoliarov.webproject.model.entity.periodical.PeriodicalOperationType.{CREATE, UPDATE}
import com.ostoliarov.webproject.model.entity.periodical.PeriodicalStatus.{ACTIVE, DISCARDED, INACTIVE, PeriodicalStatus}
import com.ostoliarov.webproject.model.entity.periodical.{Periodical, PeriodicalOperationType}
import com.ostoliarov.webproject.service.impl.mysql.PeriodicalServiceMysqlImpl
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
	* Created by Oleg Stoliarov on 10/11/18.
	* { @code create} and
	* { @code update} operations by analysing { @code periodicalOperationType} request parameter.
	*/
object PersistOnePeriodical extends RequestProcessor {
	private type ParamName = String
	private val LOGGER = LoggerFactory.getLogger(PersistOnePeriodical.getClass)
	private val periodicalService = PeriodicalServiceMysqlImpl
	private val messageFactory = FrontMessageFactory

	override def process(request: HttpServletRequest,
											 response: HttpServletResponse): ResourceRequest = {
		val generalMessages = mutable.ListBuffer[FrontendMessage]()
		val periodicalToSave = periodicalFromRequest(request)
		val redirectUri = getRedirectUriByOperationType(request, periodicalToSave)

		request.getSession.setAttribute(PERIODICAL_ATTR_NAME, periodicalToSave)

		if (isPeriodicalToSaveValid(periodicalToSave, request))
			generalMessages += messageFactory.info(MSG_VALIDATION_PASSED_SUCCESS)
		else
			return ResourceRequest(REDIRECT, AbstractViewName(redirectUri))

		try {
			checkPeriodicalForActiveSubscriptions(
				periodicalToSave,
				statusChange = PeriodicalStatusChange.instance(periodicalToSave),
				generalMessages
			)

			if (isStatusChangedFromActiveOrInactiveToDiscarded(PeriodicalStatusChange.instance(periodicalToSave))) {
				val affectedRows: Int = periodicalService.updateAndSetDiscarded(periodicalToSave)
				if (affectedRows == 0) {
					addErrorMessage(MSG_PERIODICAL_HAS_ACTIVE_SUBSCRIPTIONS_ERROR, generalMessages, request)

					return ResourceRequest(REDIRECT, AbstractViewName(redirectUri))
				}
			} else periodicalService.save(periodicalToSave)

			addMessagesToSession(request, generalMessages)

			return DisplayAllPeriodicals.process(request, response)
		} catch {
			case e: RuntimeException =>
				LOGGER.error(s"Exception during persisting periodical ($periodicalToSave).", e)
				addErrorMessage(MSG_PERIODICAL_PERSISTING_ERROR, generalMessages, request)
				return ResourceRequest(REDIRECT, AbstractViewName(redirectUri))
		}
	}

	private def addErrorMessage(message: String,
															generalMessages: mutable.ListBuffer[FrontendMessage],
															request: HttpServletRequest): Unit = {
		generalMessages += messageFactory.error(message)
		addGeneralMessagesToSession(request, generalMessages)
	}

	private def periodicalToSaveHasActiveSubscriptions(periodicalToSave: Periodical) =
		periodicalService.hasActiveSubscriptions(periodicalToSave.id)

	private def getOperationTypeFromRequest(request: HttpServletRequest) =
		PeriodicalOperationType.withName(
			request.getParameter(PERIODICAL_OPERATION_TYPE_PARAM_ATTR_NAME).toUpperCase
		)

	private def checkPeriodicalForActiveSubscriptions(periodicalToSave: Periodical,
																										statusChange: PeriodicalStatusChange,
																										generalMessages: mutable.ListBuffer[FrontendMessage]): Unit = {
		if (isStatusChangedFromActiveToInactive(statusChange)
			&& periodicalToSaveHasActiveSubscriptions(periodicalToSave))
			generalMessages += messageFactory.warning(MSG_PERIODICAL_HAS_ACTIVE_SUBSCRIPTIONS_WARNING)
	}

	private def addMessagesToSession(request: HttpServletRequest,
																	 generalMessages: mutable.ListBuffer[FrontendMessage]): Unit = {
		getOperationTypeFromRequest(request) match {
			case CREATE =>
				generalMessages += messageFactory.success(MSG_PERIODICAL_CREATED_SUCCESS)
			case UPDATE =>
				generalMessages += messageFactory.success(MSG_PERIODICAL_UPDATED_SUCCESS)
			case _ =>
				throw new IllegalArgumentException(INCORRECT_OPERATION_DURING_PERSISTING_A_PERIODICAL)
		}

		addGeneralMessagesToSession(request, generalMessages)
	}

	private def isStatusChangedFromActiveOrInactiveToDiscarded(statusChange: PeriodicalStatusChange) = {
		val oldStatus = statusChange.oldStatus
		val newStatus = statusChange.newStatus

		((ACTIVE == oldStatus || INACTIVE == oldStatus)
			&& (DISCARDED == newStatus))
	}

	private def isStatusChangedFromActiveToInactive(statusChange: PeriodicalStatusChange) =
		((ACTIVE == statusChange.oldStatus)
			&& (INACTIVE == statusChange.newStatus))

	private def getRedirectUriByOperationType(request: HttpServletRequest, periodicalToSave: Periodical) =
		getOperationTypeFromRequest(request) match {
			case CREATE => PERIODICAL_CREATE_NEW_URI
			case UPDATE => PERIODICAL_LIST_URI + "/" + periodicalToSave.id + "/update"
			case _ =>
				throw new IllegalArgumentException(INCORRECT_OPERATION_DURING_PERSISTING_A_PERIODICAL)
		}

	private def isPeriodicalToSaveValid(periodicalToSave: Periodical, request: HttpServletRequest) = {
		val messages = mutable.Map[ParamName, FrontendMessage]()
		validateName(periodicalToSave, request, messages)
		validateCategory(periodicalToSave, request, messages)
		validatePublisher(periodicalToSave, request, messages)
		validateCost(periodicalToSave, request, messages)

		if (messages.nonEmpty)
			request.getSession.setAttribute(MESSAGES_ATTR_NAME, messages.asJava)

		messages.isEmpty
	}

	private def validateName(periodicalToSave: Periodical,
													 request: HttpServletRequest,
													 messages: mutable.Map[ParamName, FrontendMessage]): Unit = {
		val result = ValidatorFactory.periodicalNameValidator.validate(periodicalToSave.name, request)
		if (result.statusCode != STATUS_CODE_SUCCESS)
			messages.put(PERIODICAL_NAME_PARAM_NAME, messageFactory.error(result.messageKey))
	}

	private def validateCategory(periodicalToSave: Periodical,
															 request: HttpServletRequest,
															 messages: mutable.Map[ParamName, FrontendMessage]): Unit = {
		val result = ValidatorFactory.periodicalCategoryValidator.validate(periodicalToSave.category.toString, request)
		if (result.statusCode != STATUS_CODE_SUCCESS)
			messages.put(PERIODICAL_NAME_PARAM_NAME, messageFactory.error(result.messageKey))
	}

	private def validatePublisher(periodicalToSave: Periodical,
																request: HttpServletRequest,
																messages: mutable.Map[ParamName, FrontendMessage]): Unit = {
		val result = ValidatorFactory.periodicalPublisherValidator.validate(periodicalToSave.publisher, request)
		if (result.statusCode != STATUS_CODE_SUCCESS)
			messages.put(PERIODICAL_PUBLISHER_PARAM_NAME, messageFactory.error(result.messageKey))
	}

	private def validateCost(periodicalToSave: Periodical,
													 request: HttpServletRequest,
													 messages: mutable.Map[ParamName, FrontendMessage]): Unit = {
		val result = ValidatorFactory.periodicalCostValidator.validate(String.valueOf(periodicalToSave.oneMonthCost), request)
		if (result.statusCode != STATUS_CODE_SUCCESS)
			messages.put(PERIODICAL_COST_PARAM_NAME, messageFactory.error(result.messageKey))
	}

	private object PeriodicalStatusChange {
		private val cache = mutable.Map[ParamName, PeriodicalStatusChange]()

		private[periodical] def instance(periodicalToSave: Periodical) = {
			val oldStatus = periodicalService.findOneById(periodicalToSave.id) match {
				case Some(periodical) => Some(periodical.status)
				case None => None
			}

			val newStatus = Option(periodicalToSave.status)
			val cacheKey = convertStatusesToCacheKey(oldStatus, newStatus)
			if (!cache.contains(cacheKey))
				cache.put(cacheKey, new PersistOnePeriodical.PeriodicalStatusChange(oldStatus.orNull, newStatus.orNull))

			cache(cacheKey)
		}

		private def convertStatusesToCacheKey(oldStatusOpt: Option[PeriodicalStatus],
																					newStatusOpt: Option[PeriodicalStatus]) =
			(oldStatusOpt, newStatusOpt) match {
				case (Some(oldStatus), Some(newStatus)) => oldStatus.toString + newStatus.toString
				case (Some(oldStatus), None) => oldStatus.toString + "null"
				case (None, Some(newStatus)) => "null" + newStatus.toString
				case (None, None) => "null" + "null"
			}
	}

	final private class PeriodicalStatusChange private(var _oldStatus: PeriodicalStatus,
																										 var _newStatus: PeriodicalStatus) {
		private[periodical] def oldStatus = _oldStatus

		private[periodical] def newStatus = _newStatus
	}

}
