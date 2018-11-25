package com.ostoliarov.webproject.controller.message

import com.ostoliarov.webproject.controller.message.MessageType._

import scala.collection.mutable

/**
	* Created by Oleg Stoliarov on 10/9/18.
	* Contains methods for generating different types of frontend messages: 'success', 'info', 'warning', 'error'.
	* Implements the Flyweight Design Pattern.
	*/
object FrontMessageFactory {
	private type MessageKey = String
	private val messagesSuccess = mutable.Map[MessageKey, FrontendMessage]()
	private val messagesInfo = mutable.Map[MessageKey, FrontendMessage]()
	private val messagesWarning = mutable.Map[MessageKey, FrontendMessage]()
	private val messagesError = mutable.Map[MessageKey, FrontendMessage]()

	def success(messageKey: MessageKey): FrontendMessage =
		updateCacheAndGetMessageFromCache(cache = messagesSuccess, messageType = SUCCESS, messageKey)

	def info(messageKey: MessageKey): FrontendMessage =
		updateCacheAndGetMessageFromCache(cache = messagesInfo, messageType = INFO, messageKey)

	def warning(messageKey: MessageKey): FrontendMessage =
		updateCacheAndGetMessageFromCache(cache = messagesWarning, messageType = WARNING, messageKey)

	def error(messageKey: MessageKey): FrontendMessage =
		updateCacheAndGetMessageFromCache(cache = messagesError, messageType = ERROR, messageKey)

	private def updateCacheAndGetMessageFromCache(cache: mutable.Map[MessageKey, FrontendMessage],
																								messageType: MessageType.MessageType,
																								messageKey: MessageKey): FrontendMessage = {
		if (!cache.contains(messageKey))
			cache += (messageKey -> FrontendMessage(messageKey, messageType))

		cache(messageKey)
	}
}
