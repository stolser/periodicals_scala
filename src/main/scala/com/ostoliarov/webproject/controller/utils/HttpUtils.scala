package com.ostoliarov.webproject.controller.utils

import java.io.IOException
import java.security.MessageDigest
import java.util.Objects.nonNull
import java.util.{NoSuchElementException, List => JavaList}

import com.ostoliarov.webproject.controller.ApplicationResources._
import com.ostoliarov.webproject.controller.message.FrontendMessage
import com.ostoliarov.webproject.controller.security.AccessDeniedException
import com.ostoliarov.webproject.dao.exception.DaoException
import com.ostoliarov.webproject.model.entity.periodical.{Periodical, PeriodicalCategory, PeriodicalStatus}
import com.ostoliarov.webproject.model.entity.user.User
import com.ostoliarov.webproject.service.impl.mysql.UserServiceMysqlImpl
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
	* Created by Oleg Stoliarov on 10/13/18.
	*/
object HttpUtils extends HttpUtilsTrait {
	private val PAGE_404_ERROR_MESSAGE = "There is no a requested resource in the system."
	private val GENERAL_ERROR_MESSAGE = "Something went wrong :( Please, repeat your action a bit later."
	private val ALGORITHM_NAME = "MD5"
	private val URI_MUST_CONTAIN_ID_TEXT = "Uri (%s) must contain id."
	private val NUMBER_REGEX = "\\d+"
	private val userService = UserServiceMysqlImpl

	/**
		* Retrieves a current user's id from the session.
		*
		* @return id of the current signed in user or 0 if a user has not been authenticated yet
		*/
	override def userIdFromSession(request: HttpServletRequest): Long = {
		val user: User = request.getSession.getAttribute(CURRENT_USER_ATTR_NAME).asInstanceOf[User]
		if (nonNull(user)) user.id
		else 0
	}

	/**
		* Retrieves a user object from the db for the current user from the request.
		*/
	override def currentUserFromFromDb(request: HttpServletRequest): Option[User] =
		userService.findOneById(userIdFromSession(request))

	/**
		* Creates a new periodical using the data from the request.
		*/
	def periodicalFromRequest(request: HttpServletRequest): Periodical =
		Periodical(
			id = java.lang.Long.parseLong(request.getParameter(ENTITY_ID_PARAM_NAME)),
			name = request.getParameter(PERIODICAL_NAME_PARAM_NAME),
			category = PeriodicalCategory.withName(request.getParameter(PERIODICAL_CATEGORY_PARAM_NAME).toUpperCase),
			publisher = request.getParameter(PERIODICAL_PUBLISHER_PARAM_NAME),
			description = Option(request.getParameter(PERIODICAL_DESCRIPTION_PARAM_NAME).trim),
			oneMonthCost = java.lang.Long.parseLong(request.getParameter(PERIODICAL_COST_PARAM_NAME)),
			status = PeriodicalStatus.withName(request.getParameter(PERIODICAL_STATUS_PARAM_NAME).toUpperCase)
		)

	/**
		* Tries to find the first number in the uri.
		*/
	override def firstIdFromUri(uri: String): Int =
		NUMBER_REGEX.r.findFirstIn(uri) match {
			case None => throw new IllegalArgumentException(URI_MUST_CONTAIN_ID_TEXT.format(uri))
			case Some(number) => number.toInt
		}

	/**
		* Sets a session scoped attribute 'messages'.
		*/
	def addMessagesToSession(request: HttpServletRequest,
													 frontMessageMap: Map[String, JavaList[FrontendMessage]]): Unit =
		request.getSession.setAttribute(MESSAGES_ATTR_NAME, frontMessageMap.asJava)

	/**
		* Adds general messages to the session.
		*/
	def addGeneralMessagesToSession(request: HttpServletRequest,
																	generalMessages: mutable.ListBuffer[FrontendMessage]): Unit =
		addMessagesToSession(
			request,
			Map(GENERAL_MESSAGES_FRONT_BLOCK_NAME -> generalMessages.toList.asJava)
		)

	def tryToSendRedirect(request: HttpServletRequest,
												response: HttpServletResponse,
												redirectUri: String): Unit =
		try
			response.sendRedirect(redirectUri)
		catch {
			case e: IOException =>
				val message: String = s"User id = ${userIdFromSession(request)}. Exception during redirection to '$redirectUri'."
				throw new RuntimeException(message, e)
		}

	/**
		* Returns an appropriate view name for this exception.
		*/
	def errorViewNameAndOriginalMessage(exception: Throwable): (String, String) =
		exception match {
			case DaoException(message, _) => (STORAGE_EXCEPTION_PAGE_VIEW_NAME, message)
			case AccessDeniedException(message) => (ACCESS_DENIED_PAGE_VIEW_NAME, message)
			case _: NoSuchElementException => (PAGE_404_VIEW_NAME, PAGE_404_ERROR_MESSAGE)
			case _ => (GENERAL_ERROR_PAGE_VIEW_NAME, GENERAL_ERROR_MESSAGE)
		}

	/**
		* Returns a hash for this password.
		*/
	def passwordHash(password: String): String = {
		val md = MessageDigest.getInstance(ALGORITHM_NAME)
		md.update(password.getBytes)
		val builder: StringBuilder = new StringBuilder
		for (aByteData <- md.digest) {
			builder.append(Integer.toString((aByteData & 0xff) + 0x100, 16).substring(1))
		}

		builder.toString
	}

	def filterRequestByHttpMethod(request: HttpServletRequest, mapping: String): Boolean = {
		val methodPattern: String = mapping.split(METHODS_URI_SEPARATOR)(0)
		val methods: Array[String] = methodPattern.split(METHOD_METHOD_SEPARATOR)
		val requestMethod: String = request.getMethod.toUpperCase
		methods.contains(requestMethod)
	}

	def filterRequestByUri(request: HttpServletRequest, mapping: String): Boolean = {
		val urlPattern = mapping.split(METHODS_URI_SEPARATOR)(1)
		request.getRequestURI.matches(urlPattern)
	}
}
