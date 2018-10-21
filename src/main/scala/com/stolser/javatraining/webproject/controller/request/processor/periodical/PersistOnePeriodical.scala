package com.stolser.javatraining.webproject.controller.request.processor.periodical

import java.util
import java.util.Objects.nonNull
import java.util.{ArrayList, HashMap, List, Map}

import com.stolser.javatraining.webproject.controller.ApplicationResources._
import com.stolser.javatraining.webproject.controller.form.validator.{ValidationResult, ValidatorFactory}
import com.stolser.javatraining.webproject.controller.message.{FrontMessageFactory, FrontendMessage}
import com.stolser.javatraining.webproject.controller.request.processor.RequestProcessor
import com.stolser.javatraining.webproject.controller.utils.HttpUtils
import com.stolser.javatraining.webproject.model.entity.periodical.{Periodical, PeriodicalOperationType, PeriodicalStatus}
import com.stolser.javatraining.webproject.model.entity.periodical.PeriodicalOperationType.{CREATE, UPDATE}
import com.stolser.javatraining.webproject.model.entity.periodical.PeriodicalStatus.{ACTIVE, DISCARDED, INACTIVE}
import com.stolser.javatraining.webproject.service.PeriodicalService
import com.stolser.javatraining.webproject.service.impl.PeriodicalServiceImpl
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.slf4j.{Logger, LoggerFactory}

/**
  * Created by Oleg Stoliarov on 10/11/18.
  * { @code create} and
  * { @code update} operations by analysing { @code periodicalOperationType} request parameter.
  */
object PersistOnePeriodical extends RequestProcessor {
	private val LOGGER = LoggerFactory.getLogger(PersistOnePeriodical.getClass)
	private val periodicalService = PeriodicalServiceImpl
	private val messageFactory = FrontMessageFactory

	override def process(request: HttpServletRequest, response: HttpServletResponse): String = {
		val generalMessages: util.List[FrontendMessage] = new util.ArrayList[FrontendMessage]
		val periodicalToSave: Periodical = HttpUtils.getPeriodicalFromRequest(request)
		val redirectUri: String = getRedirectUriByOperationType(request, periodicalToSave)
		request.getSession.setAttribute(PERIODICAL_ATTR_NAME, periodicalToSave)

		if (isPeriodicalToSaveValid(periodicalToSave, request)) generalMessages.add(messageFactory.getInfo(MSG_VALIDATION_PASSED_SUCCESS))
		else return REDIRECT + redirectUri

		try {
			checkPeriodicalForActiveSubscriptions(periodicalToSave, PeriodicalStatusChange.getInstance(periodicalToSave), generalMessages)
			if (isStatusChangedFromActiveOrInactiveToDiscarded(PeriodicalStatusChange.getInstance(periodicalToSave))) {
				val affectedRows: Int = periodicalService.updateAndSetDiscarded(periodicalToSave)
				if (affectedRows == 0) {
					addErrorMessage(MSG_PERIODICAL_HAS_ACTIVE_SUBSCRIPTIONS_ERROR, generalMessages, request)
					return REDIRECT + redirectUri
				}
			}
			else periodicalService.save(periodicalToSave)
			addGeneralMessagesToSession(request, generalMessages)
			return DisplayAllPeriodicals.process(request, response)
		} catch {
			case e: RuntimeException =>
				LOGGER.error(s"Exception during persisting periodical ($periodicalToSave).", e)
				addErrorMessage(MSG_PERIODICAL_PERSISTING_ERROR, generalMessages, request)
				return REDIRECT + redirectUri
		}
	}

	private def addErrorMessage(message: String,
								generalMessages: util.List[FrontendMessage],
								request: HttpServletRequest): Unit = {
		generalMessages.add(messageFactory.getError(message))
		HttpUtils.addGeneralMessagesToSession(request, generalMessages)
	}

	private def periodicalToSaveHasActiveSubscriptions(periodicalToSave: Periodical) =
		periodicalService.hasActiveSubscriptions(periodicalToSave.getId)

	private def getOperationTypeFromRequest(request: HttpServletRequest) =
		PeriodicalOperationType.withName(request.getParameter(PERIODICAL_OPERATION_TYPE_PARAM_ATTR_NAME).toUpperCase)

	private def checkPeriodicalForActiveSubscriptions(periodicalToSave: Periodical,
													  statusChange: PersistOnePeriodical.PeriodicalStatusChange,
													  generalMessages: util.List[FrontendMessage]): Unit = {
		if (isStatusChangedFromActiveToInactive(statusChange) &&
			periodicalToSaveHasActiveSubscriptions(periodicalToSave))
			generalMessages.add(messageFactory.getWarning(MSG_PERIODICAL_HAS_ACTIVE_SUBSCRIPTIONS_WARNING))
	}

	private def addGeneralMessagesToSession(request: HttpServletRequest,
											generalMessages: util.List[FrontendMessage]): Unit = {
		getOperationTypeFromRequest(request) match {
			case CREATE =>
				generalMessages.add(messageFactory.getSuccess(MSG_PERIODICAL_CREATED_SUCCESS))
			case UPDATE =>
				generalMessages.add(messageFactory.getSuccess(MSG_PERIODICAL_UPDATED_SUCCESS))
			case _ =>
				throw new IllegalArgumentException(INCORRECT_OPERATION_DURING_PERSISTING_A_PERIODICAL)
		}

		HttpUtils.addGeneralMessagesToSession(request, generalMessages)
	}

	private def isStatusChangedFromActiveOrInactiveToDiscarded(statusChange: PeriodicalStatusChange) = {
		val oldStatus = statusChange.getOldStatus
		val newStatus = statusChange.getNewStatus
		(ACTIVE == oldStatus || INACTIVE == oldStatus) &&
			(DISCARDED == newStatus)
	}

	private def isStatusChangedFromActiveToInactive(statusChange: PeriodicalStatusChange) =
		(ACTIVE == statusChange.getOldStatus) &&
			(INACTIVE == statusChange.getNewStatus)

	private def getRedirectUriByOperationType(request: HttpServletRequest, periodicalToSave: Periodical) = {
		getOperationTypeFromRequest(request) match {
			case CREATE => PERIODICAL_CREATE_NEW_URI
			case UPDATE => PERIODICAL_LIST_URI + "/" + periodicalToSave.getId + "/update"
			case _ =>
				throw new IllegalArgumentException(INCORRECT_OPERATION_DURING_PERSISTING_A_PERIODICAL)
		}
	}

	private def isPeriodicalToSaveValid(periodicalToSave: Periodical, request: HttpServletRequest) = {
		val messages = new util.HashMap[String, FrontendMessage]
		validateName(periodicalToSave, request, messages)
		validateCategory(periodicalToSave, request, messages)
		validatePublisher(periodicalToSave, request, messages)
		validateCost(periodicalToSave, request, messages)
		val messagesSize = messages.size
		if (messagesSize > 0) request.getSession.setAttribute(MESSAGES_ATTR_NAME, messages)
		messagesSize == 0
	}

	private def validateName(periodicalToSave: Periodical, request: HttpServletRequest, messages: util.Map[String, FrontendMessage]): Unit = {
		val result = ValidatorFactory.getPeriodicalNameValidator.validate(periodicalToSave.getName, request)
		if (result.statusCode != STATUS_CODE_SUCCESS)
			messages.put(PERIODICAL_NAME_PARAM_NAME, messageFactory.getError(result.messageKey))
	}

	private def validateCategory(periodicalToSave: Periodical, request: HttpServletRequest, messages: util.Map[String, FrontendMessage]): Unit = {
		val result = ValidatorFactory.getPeriodicalCategoryValidator.validate(periodicalToSave.getCategory.toString, request)
		if (result.statusCode != STATUS_CODE_SUCCESS)
			messages.put(PERIODICAL_NAME_PARAM_NAME, messageFactory.getError(result.messageKey))
	}

	private def validatePublisher(periodicalToSave: Periodical, request: HttpServletRequest, messages: util.Map[String, FrontendMessage]): Unit = {
		val result = ValidatorFactory.getPeriodicalPublisherValidator.validate(periodicalToSave.getPublisher, request)
		if (result.statusCode != STATUS_CODE_SUCCESS)
			messages.put(PERIODICAL_PUBLISHER_PARAM_NAME, messageFactory.getError(result.messageKey))
	}

	private def validateCost(periodicalToSave: Periodical, request: HttpServletRequest, messages: util.Map[String, FrontendMessage]): Unit = {
		val result = ValidatorFactory.getPeriodicalCostValidator.validate(String.valueOf(periodicalToSave.getOneMonthCost), request)
		if (result.statusCode != STATUS_CODE_SUCCESS)
			messages.put(PERIODICAL_COST_PARAM_NAME, messageFactory.getError(result.messageKey))
	}

	private object PeriodicalStatusChange {
		private val cache = new util.HashMap[String, PeriodicalStatusChange]

		private[periodical] def getInstance(periodicalToSave: Periodical) = {
			val periodicalInDb = PeriodicalServiceImpl.findOneById(periodicalToSave.getId)
			val oldStatus = if (nonNull(periodicalInDb)) periodicalInDb.getStatus else null
			val newStatus = periodicalToSave.getStatus
			val cacheKey = getCacheKey(oldStatus, newStatus)
			if (!cache.containsKey(cacheKey))
				cache.put(cacheKey, new PersistOnePeriodical.PeriodicalStatusChange(oldStatus, newStatus))

			cache.get(cacheKey)
		}

		private def getCacheKey(oldStatus: PeriodicalStatus.Value, newStatus: PeriodicalStatus.Value) =
			(if (nonNull(oldStatus)) oldStatus.toString	else "null") +
				(if (nonNull(newStatus)) newStatus.toString	else "null")
	}

	final private class PeriodicalStatusChange private(var oldStatus: PeriodicalStatus.Value,
													   var newStatus: PeriodicalStatus.Value) {
		private[periodical] def getOldStatus = oldStatus
		private[periodical] def getNewStatus = newStatus
	}

}
