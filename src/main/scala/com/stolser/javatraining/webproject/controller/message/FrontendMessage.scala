package com.stolser.javatraining.webproject.controller.message

import scala.beans.BeanProperty

/**
	* Created by Oleg Stoliarov on 10/9/18.
	* Encapsulates data about a message displayed on the frontend.
	*/
final class FrontendMessage private(@BeanProperty val messageKey: String,
																		@BeanProperty val messageType: FrontendMessage.MessageType.Value) {

	override def toString: String = s"FrontendMessage{messageKey='$messageKey', messageType=$messageType}"
}

object FrontendMessage {

	object MessageType extends Enumeration {
		val SUCCESS, INFO, WARNING, ERROR = Value
	}

	def apply(messageKey: String, messageType: MessageType.Value): FrontendMessage =
		new FrontendMessage(messageKey, messageType)
}

