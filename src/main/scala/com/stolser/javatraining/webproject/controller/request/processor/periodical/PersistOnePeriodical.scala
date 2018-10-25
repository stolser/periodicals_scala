package com.stolser.javatraining.webproject.controller.request.processor.periodical

import java.util.Objects.nonNull

import com.stolser.javatraining.webproject.controller.ApplicationResources._
import com.stolser.javatraining.webproject.controller.form.validator.ValidatorFactory
import com.stolser.javatraining.webproject.controller.message.{FrontMessageFactory, FrontendMessage}
import com.stolser.javatraining.webproject.controller.request.processor.RequestProcessor
import com.stolser.javatraining.webproject.controller.utils.HttpUtils
import com.stolser.javatraining.webproject.controller.utils.HttpUtils._
import com.stolser.javatraining.webproject.model.entity.periodical.PeriodicalOperationType.{CREATE, UPDATE}
import com.stolser.javatraining.webproject.model.entity.periodical.PeriodicalStatus.{ACTIVE, DISCARDED, INACTIVE}
import com.stolser.javatraining.webproject.model.entity.periodical.{Periodical, PeriodicalOperationType, PeriodicalStatus}
import com.stolser.javatraining.webproject.service.impl.PeriodicalServiceImpl
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
	private val LOGGER = LoggerFactory.getLogger(PersistOnePeriodical.getClass)
	private val periodicalService = PeriodicalServiceImpl
	private val messageFactory = FrontMessageFactory

	override def process(request: HttpServletRequest, response: HttpServletResponse): String = {
		val generalMessages = mutable.ListBuffer[FrontendMessage]()
		val periodicalToSave = getPeriodicalFromRequest(request)
		val redirectUri = getRedirectUriByOperationType(request, periodicalToSave)

		request.getSession.setAttribute(PERIODICAL_ATTR_NAME, periodicalToSave)

		if (isPeriodicalToSaveValid(periodicalToSave, request))
			generalMessages += messageFactory.getInfo(MSG_VALIDATION_PASSED_SUCCESS)
		else
			return REDIRECT + redirectUri

		try {
			checkPeriodicalForActiveSubscriptions(
				periodicalToSave,
				statusChange = PeriodicalStatusChange.getInstance(periodicalToSave),
				generalMessages
			)

			if (isStatusChangedFromActiveOrInactiveToDiscarded(PeriodicalStatusChange.getInstance(periodicalToSave))) {
				val affectedRows: Int = periodicalService.updateAndSetDiscarded(periodicalToSave)
				if (affectedRows == 0) {
					addErrorMessage(MSG_PERIODICAL_HAS_ACTIVE_SUBSCRIPTIONS_ERROR, generalMessages, request)
					return REDIRECT + redirectUri
				}
			}
			else periodicalService.save(periodicalToSave)

			addMessagesToSession(request, generalMessages)

			return DisplayAllPeriodicals.process(request, response)
		} catch {
			case e: RuntimeException =>
				LOGGER.error(s"Exception during persisting periodical ($periodicalToSave).", e)
				addErrorMessage(MSG_PERIODICAL_PERSISTING_ERROR, generalMessages, request)
				return REDIRECT + redirectUri
		}
	}

	private def addErrorMessage(message: String,
								generalMessages: mutable.ListBuffer[FrontendMessage],
								request: HttpServletRequest): Unit = {
		generalMessages += messageFactory.getError(message)
		HttpUtils.addGeneralMessagesToSession(request, generalMessages)
	}

	private def periodicalToSaveHasActiveSubscriptions(periodicalToSave: Periodical) =
		periodicalService.hasActiveSubscriptions(periodicalToSave.id)

	private def getOperationTypeFromRequest(request: HttpServletRequest) =
		PeriodicalOperationType.withName(
			request.getParameter(PERIODICAL_OPERATION_TYPE_PARAM_ATTR_NAME).toUpperCase
		)

	private def checkPeriodicalForActiveSubscriptions(periodicalToSave: Periodical,
													  statusChange: PersistOnePeriodical.PeriodicalStatusChange,
													  generalMessages: mutable.ListBuffer[FrontendMessage]): Unit = {
		if (isStatusChangedFromActiveToInactive(statusChange) &&
			periodicalToSaveHasActiveSubscriptions(periodicalToSave))
			generalMessages += messageFactory.getWarning(MSG_PERIODICAL_HAS_ACTIVE_SUBSCRIPTIONS_WARNING)
	}

	private def addMessagesToSession(request: HttpServletRequest,
											generalMessages: mutable.ListBuffer[FrontendMessage]): Unit = {
		getOperationTypeFromRequest(request) match {
			case CREATE =>
				generalMessages += messageFactory.getSuccess(MSG_PERIODICAL_CREATED_SUCCESS)
			case UPDATE =>
				generalMessages += messageFactory.getSuccess(MSG_PERIODICAL_UPDATED_SUCCESS)
			case _ =>
				throw new IllegalArgumentException(INCORRECT_OPERATION_DURING_PERSISTING_A_PERIODICAL)
		}

		addGeneralMessagesToSession(request, generalMessages)
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

	private def getRedirectUriByOperationType(request: HttpServletRequest, periodicalToSave: Periodical) =
		getOperationTypeFromRequest(request) match {
			case CREATE => PERIODICAL_CREATE_NEW_URI
			case UPDATE => PERIODICAL_LIST_URI + "/" + periodicalToSave.id + "/update"
			case _ =>
				throw new IllegalArgumentException(INCORRECT_OPERATION_DURING_PERSISTING_A_PERIODICAL)
		}

	private def isPeriodicalToSaveValid(periodicalToSave: Periodical, request: HttpServletRequest) = {
		val messages = mutable.Map[String, FrontendMessage]()
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
							 messages: mutable.Map[String, FrontendMessage]): Unit = {
		val result = ValidatorFactory.getPeriodicalNameValidator.validate(periodicalToSave.name, request)
		if (result.statusCode != STATUS_CODE_SUCCESS)
			messages.put(PERIODICAL_NAME_PARAM_NAME, messageFactory.getError(result.messageKey))
	}

	private def validateCategory(periodicalToSave: Periodical,
								 request: HttpServletRequest,
								 messages: mutable.Map[String, FrontendMessage]): Unit = {
		val result = ValidatorFactory.getPeriodicalCategoryValidator.validate(periodicalToSave.category.toString, request)
		if (result.statusCode != STATUS_CODE_SUCCESS)
			messages.put(PERIODICAL_NAME_PARAM_NAME, messageFactory.getError(result.messageKey))
	}

	private def validatePublisher(periodicalToSave: Periodical,
								  request: HttpServletRequest,
								  messages: mutable.Map[String, FrontendMessage]): Unit = {
		val result = ValidatorFactory.getPeriodicalPublisherValidator.validate(periodicalToSave.publisher, request)
		if (result.statusCode != STATUS_CODE_SUCCESS)
			messages.put(PERIODICAL_PUBLISHER_PARAM_NAME, messageFactory.getError(result.messageKey))
	}

	private def validateCost(periodicalToSave: Periodical,
							 request: HttpServletRequest,
							 messages: mutable.Map[String, FrontendMessage]): Unit = {
		val result = ValidatorFactory.getPeriodicalCostValidator.validate(String.valueOf(periodicalToSave.oneMonthCost), request)
		if (result.statusCode != STATUS_CODE_SUCCESS)
			messages.put(PERIODICAL_COST_PARAM_NAME, messageFactory.getError(result.messageKey))
	}

	private object PeriodicalStatusChange {
		private val cache = mutable.Map[String, PeriodicalStatusChange]()

		private[periodical] def getInstance(periodicalToSave: Periodical) = {
			val periodicalInDb = PeriodicalServiceImpl.findOneById(periodicalToSave.id)
			val oldStatus =
				if (nonNull(periodicalInDb))
					periodicalInDb.status
				else null
			val newStatus = periodicalToSave.status
			val cacheKey = getCacheKey(oldStatus, newStatus)
			if (!cache.contains(cacheKey))
				cache.put(cacheKey, new PersistOnePeriodical.PeriodicalStatusChange(oldStatus, newStatus))

			cache(cacheKey)
		}

		private def getCacheKey(oldStatus: PeriodicalStatus.Value,
								newStatus: PeriodicalStatus.Value) =
			(if (nonNull(oldStatus)) oldStatus.toString else "null") +
				(if (nonNull(newStatus)) newStatus.toString else "null")
	}

	final private class PeriodicalStatusChange private(var oldStatus: PeriodicalStatus.Value,
													   var newStatus: PeriodicalStatus.Value) {
		private[periodical] def getOldStatus = oldStatus
		private[periodical] def getNewStatus = newStatus
	}
}
