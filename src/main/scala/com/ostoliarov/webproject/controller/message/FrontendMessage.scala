package com.ostoliarov.webproject.controller.message

import scala.beans.BeanProperty

/**
	* Created by Oleg Stoliarov on 10/9/18.
	* Encapsulates data about a message displayed on the frontend.
	*/
case class FrontendMessage private(@BeanProperty messageKey: String,
																	 @BeanProperty messageType: FrontendMessage.MessageType.Value) {}

object FrontendMessage {

	object MessageType extends Enumeration {
		val SUCCESS, INFO, WARNING, ERROR = Value
	}

}

