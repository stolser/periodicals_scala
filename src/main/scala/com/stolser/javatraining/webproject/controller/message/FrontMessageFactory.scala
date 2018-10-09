package com.stolser.javatraining.webproject.controller.message

import com.stolser.javatraining.webproject.controller.message.FrontendMessage.MessageType._

import scala.collection.mutable

/**
  * Created by Oleg Stoliarov on 10/9/18.
  * Contains methods for generating different types of frontend messages: 'success', 'info', 'warning', 'error'.
  * Implements the Flyweight Design Pattern.
  */
object FrontMessageFactory {
	private val messagesSuccess = mutable.Map[String, FrontendMessage]()
	private val messagesInfo = mutable.Map[String, FrontendMessage]()
	private val messagesWarning = mutable.Map[String, FrontendMessage]()
	private val messagesError = mutable.Map[String, FrontendMessage]()

	def getSuccess(messageKey: String): FrontendMessage = getMessageFromCache(messagesSuccess, SUCCESS, messageKey)

	def getInfo(messageKey: String): FrontendMessage = getMessageFromCache(messagesInfo, INFO, messageKey)

	def getWarning(messageKey: String): FrontendMessage = getMessageFromCache(messagesWarning, WARNING, messageKey)

	def getError(messageKey: String): FrontendMessage = getMessageFromCache(messagesError, ERROR, messageKey)

	private def getMessageFromCache(cache: mutable.Map[String, FrontendMessage],
									messageType: FrontendMessage.MessageType.Value,
									messageKey: String): FrontendMessage = {
		if (!cache.contains(messageKey))
			cache += (messageKey -> FrontendMessage(messageKey, messageType))

		cache(messageKey)
	}
}
