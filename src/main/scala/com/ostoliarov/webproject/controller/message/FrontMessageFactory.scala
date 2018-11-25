package com.ostoliarov.webproject.controller.message

import com.ostoliarov.webproject.controller.message.MessageType._

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

	def success(messageKey: String): FrontendMessage =
		updateCacheAndGetMessageFromCache(cache = messagesSuccess, messageType = SUCCESS, messageKey)

	def info(messageKey: String): FrontendMessage =
		updateCacheAndGetMessageFromCache(cache = messagesInfo, messageType = INFO, messageKey)

	def warning(messageKey: String): FrontendMessage =
		updateCacheAndGetMessageFromCache(cache = messagesWarning, messageType = WARNING, messageKey)

	def error(messageKey: String): FrontendMessage =
		updateCacheAndGetMessageFromCache(cache = messagesError, messageType = ERROR, messageKey)

	private def updateCacheAndGetMessageFromCache(cache: mutable.Map[String, FrontendMessage],
																								messageType: MessageType.MessageType,
																								messageKey: String): FrontendMessage = {
		if (!cache.contains(messageKey))
			cache += (messageKey -> FrontendMessage(messageKey, messageType))

		cache(messageKey)
	}
}
