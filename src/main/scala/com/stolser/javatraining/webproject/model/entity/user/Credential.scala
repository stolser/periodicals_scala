package com.stolser.javatraining.webproject.model.entity.user

import com.google.common.base.Preconditions.checkNotNull

import scala.beans.BeanProperty

/**
	* Created by Oleg Stoliarov on 10/20/18.
	*/
case class Credential private(@BeanProperty id: Long = 0,
															@BeanProperty userName: String = "",
															@BeanProperty passwordHash: String = "",
															@BeanProperty var userId: Long = 0) {

	checkNotNull(userName)
	checkNotNull(passwordHash)

	override def toString: String = s"Credentials{id=$id, userName='$userName', userId='$userId'}"
}

object Credential {
	private[this] val emptyCredential = new Credential()

	def apply: Credential = emptyCredential
}