package com.ostoliarov.webproject.controller.request.processor

import java.util.NoSuchElementException

import com.ostoliarov.webproject.controller.ApplicationResources._
import com.ostoliarov.webproject.controller.form.validator.AjaxFormValidation
import com.ostoliarov.webproject.controller.request.processor.admin.panel.DisplayAdminPanel
import com.ostoliarov.webproject.controller.request.processor.invoice.{PayOneInvoice, PersistOneInvoice}
import com.ostoliarov.webproject.controller.request.processor.periodical._
import com.ostoliarov.webproject.controller.request.processor.sign.{DisplaySignUpPage, SignIn, SignOut}
import com.ostoliarov.webproject.controller.request.processor.user.{CreateUser, DisplayAllUsers, DisplayCurrentUser}
import com.ostoliarov.webproject.controller.utils.HttpUtils.{filterRequestByHttpMethod, filterRequestByUri}
import javax.servlet.http.HttpServletRequest

import scala.collection.mutable

/**
	* Created by Oleg Stoliarov on 10/10/18.
	* Provides mapping request uri to classes that will perform actual request processing.
	*/
object RequestProviderImpl extends RequestProvider {
	val GET_BACKEND_REQUEST_PATTERN: String = "GET:/backend/?"
	val GET_ADMIN_PANEL_REQUEST_PATTERN: String = "GET:" + ADMIN_PANEL_URI + "/?"
	val GET_ALL_USERS_REQUEST_PATTERN: String = "GET:" + USERS_LIST_URI + "/?"
	val GET_CURRENT_USER_REQUEST_PATTERN: String = "GET:" + CURRENT_USER_ACCOUNT_URI + "/?"
	val POST_SIGN_IN_REQUEST_PATTERN: String = "POST:" + SIGN_IN_URI + "/?"
	val POST_PERSIST_INVOICE_REQUEST_PATTERN: String = "POST:" + USERS_LIST_URI + "/\\d+/invoices/?"
	val POST_PAY_INVOICE_REQUEST_PATTERN: String = "POST:" + USERS_LIST_URI + "/\\d+/invoices/\\d+/pay/?"
	val GET_ONE_PERIODICAL_REQUEST_PATTERN: String = "GET:" + PERIODICAL_LIST_URI + "/\\d+"
	val GET_ALL_PERIODICALS_REQUEST_PATTERN: String = "GET:" + PERIODICAL_LIST_URI + "/?"
	val POST_PERSIST_PERIODICAL_REQUEST_PATTERN: String = "POST:" + PERIODICAL_LIST_URI + "/?"
	val GET_CREATE_PERIODICAL_REQUEST_PATTERN: String = "GET:" + PERIODICAL_LIST_URI + "/createNew/?"
	val GET_UPDATE_PERIODICAL_REQUEST_PATTERN: String = "GET:" + PERIODICAL_LIST_URI + "/\\d+/update/?"
	val POST_DELETE_PERIODICALS_REQUEST_PATTERN: String = "POST:" + PERIODICAL_LIST_URI + "/discarded/?"
	val GET_SIGN_OUT_REQUEST_PATTERN: String = "GET:" + SIGN_OUT_URI + "/?"
	val POST_SIGN_UP_REQUEST_PATTERN: String = "POST:" + SIGN_UP_URI + "/?"
	val GET_SIGN_UP_REQUEST_PATTERN: String = "GET:" + SIGN_UP_URI + "/?"
	val POST_AJAX_FORM_VALIDATOR_REQUEST_PATTERN: String = "POST:/backend/validation"

	private val NO_MAPPING_FOR_SUCH_REQUEST: String = "There no mapping for such a request: '%s'."
	private val requestMapping: mutable.Map[String, RequestProcessor] = mutable.Map()

	initializeRequestMapping

	override def requestProcessor(request: HttpServletRequest): RequestProcessor = {
		val currentMapping = requestMapping.filterKeys((key: String) => filterRequestByHttpMethod(request, key))
			.filterKeys(filterRequestByUri(request, _))
			.headOption

		currentMapping match {
			case Some((_, requestProcessor)) => requestProcessor
			case None => throw new NoSuchElementException(NO_MAPPING_FOR_SUCH_REQUEST.format(request.getRequestURI))
		}
	}

	private def initializeRequestMapping = {
		requestMapping += (POST_SIGN_IN_REQUEST_PATTERN -> SignIn)
		requestMapping += (GET_BACKEND_REQUEST_PATTERN -> DisplayBackendHomePage)
		requestMapping += (GET_ADMIN_PANEL_REQUEST_PATTERN -> DisplayAdminPanel)
		requestMapping += (GET_ALL_USERS_REQUEST_PATTERN -> DisplayAllUsers)
		requestMapping += (GET_CURRENT_USER_REQUEST_PATTERN -> DisplayCurrentUser)
		requestMapping += (POST_PERSIST_INVOICE_REQUEST_PATTERN -> PersistOneInvoice)
		requestMapping += (POST_PAY_INVOICE_REQUEST_PATTERN -> PayOneInvoice)
		requestMapping += (GET_ONE_PERIODICAL_REQUEST_PATTERN -> DisplayOnePeriodical)
		requestMapping += (GET_ALL_PERIODICALS_REQUEST_PATTERN -> DisplayAllPeriodicals)
		requestMapping += (POST_PERSIST_PERIODICAL_REQUEST_PATTERN -> PersistOnePeriodical)
		requestMapping += (GET_CREATE_PERIODICAL_REQUEST_PATTERN -> DisplayNewPeriodicalPage)
		requestMapping += (GET_UPDATE_PERIODICAL_REQUEST_PATTERN -> DisplayUpdatePeriodicalPage)
		requestMapping += (POST_DELETE_PERIODICALS_REQUEST_PATTERN -> DeleteDiscardedPeriodicals)
		requestMapping += (GET_SIGN_OUT_REQUEST_PATTERN -> SignOut)
		requestMapping += (POST_SIGN_UP_REQUEST_PATTERN -> CreateUser)
		requestMapping += (GET_SIGN_UP_REQUEST_PATTERN -> DisplaySignUpPage)
		requestMapping += (POST_AJAX_FORM_VALIDATOR_REQUEST_PATTERN -> AjaxFormValidation)
	}
}
