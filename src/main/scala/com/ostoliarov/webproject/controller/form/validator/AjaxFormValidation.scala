package com.ostoliarov.webproject.controller.form.validator

import java.io.IOException
import java.util.Objects.nonNull
import java.util.{Locale, ResourceBundle}

import com.ostoliarov.webproject.controller.ApplicationResources._
import com.ostoliarov.webproject.controller.request.processor.RequestProcessor
import com.ostoliarov.webproject.view.SystemLocale
import javax.servlet.http.{HttpServletRequest, HttpServletResponse, HttpSession}
import org.json.{JSONException, JSONObject}
import org.slf4j.LoggerFactory

/**
	* Created by Oleg Stoliarov on 10/7/18.
	* Validates a parameter from the request and sends a json with the validation result.
	* Can be used for ajax validation of input field values.
	*/
object AjaxFormValidation extends RequestProcessor {
	private val LOGGER = LoggerFactory.getLogger(AjaxFormValidation.getClass)
	private val EXCEPTION_DURING_PUTTING_VALUES_INTO_JSON_OBJECT = "Exception during putting values into json object."
	private val JSON_CONTENT_TYPE = "application/json"
	private val STATUS_CODE_JSON_RESPONSE = "statusCode"
	private val VALIDATION_MESSAGE_JSON_RESPONSE = "validationMessage"
	private val EXCEPTION_DURING_VALIDATION = "Exception during validation."

	override def process(request: HttpServletRequest,
											 response: HttpServletResponse): String = {
		val session = request.getSession
		val paramName = request.getParameter(PARAM_NAME)
		val paramValue = request.getParameter(PARAM_VALUE)

		removeMessagesForCurrentParam(session, paramName)
		customizeResponse(response)

		tryToValidateRequest {
			writeJsonIntoResponse(
				response,
				jsonResponse(
					validationResult = ValidatorFactory.newValidator(paramName).validate(paramValue, request),
					session
				)
			)
		}

		NO_ACTION
	}

	private def tryToValidateRequest(block: => Unit): Unit =
		try {
			block
		} catch {
			case e: JSONException =>
				LOGGER.error(EXCEPTION_DURING_PUTTING_VALUES_INTO_JSON_OBJECT, e)
				throw ValidationProcessorException(EXCEPTION_DURING_PUTTING_VALUES_INTO_JSON_OBJECT, e)
			case e: IOException =>
				throw ValidationProcessorException(EXCEPTION_DURING_VALIDATION, e)
		}

	private def removeMessagesForCurrentParam(session: HttpSession, paramName: String): Unit = {
		var frontEndMessages: Map[String, _] = session.getAttribute(MESSAGES_ATTR_NAME).asInstanceOf[Map[String, _]]
		if (nonNull(frontEndMessages))
			frontEndMessages -= paramName
	}

	private def customizeResponse(response: HttpServletResponse): Unit = {
		response.setContentType(JSON_CONTENT_TYPE)
		response.setCharacterEncoding(CHARACTER_ENCODING)
	}

	private def jsonResponse(validationResult: ValidationResult,
													 session: HttpSession) = {
		val jsonResponse = new JSONObject
		jsonResponse.put(STATUS_CODE_JSON_RESPONSE, validationResult.statusCode)
		jsonResponse.put(VALIDATION_MESSAGE_JSON_RESPONSE, getLocalizedMessage(session, validationResult))
	}

	@throws[IOException]
	private def writeJsonIntoResponse(response: HttpServletResponse, jsonResponse: JSONObject): Unit = {
		val writer = response.getWriter
		writer.println(jsonResponse.toString)
		writer.flush()
	}

	private def getLocaleFromSession(session: HttpSession): Locale = {
		val localeAttr: AnyRef = session.getAttribute(LANGUAGE_ATTR_NAME)
		localeAttr match {
			case _: Locale => localeAttr.asInstanceOf[Locale]
			case _ => SystemLocale.withName(localeAttr.asInstanceOf[String].toUpperCase)
				.locale
		}
	}

	private def getLocalizedMessage(session: HttpSession, result: ValidationResult) =
		ResourceBundle.getBundle(VALIDATION_BUNDLE_PATH, getLocaleFromSession(session))
			.getString(result.messageKey)
}