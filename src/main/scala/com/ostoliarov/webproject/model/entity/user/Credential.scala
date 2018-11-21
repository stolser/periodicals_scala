package com.ostoliarov.webproject.model.entity.user

import com.google.common.base.Preconditions.checkNotNull

import scala.beans.BeanProperty

/**
	* Created by Oleg Stoliarov on 10/20/18.
	*/
case class Credential private(@BeanProperty id: Long = 0,
															@BeanProperty userName: String = "",
															@BeanProperty passwordHash: String = "",
															@BeanProperty var userId: Long = 0) {

	require(id >= 0)
	checkNotNull(userName)
	checkNotNull(passwordHash)
	require(userId >= 0)

	override def toString: String = s"Credentials{id=$id, userName='$userName', userId='$userId'}"
}

object Credential {
	private[this] val emptyCredential = new Credential()

	def apply: Credential = emptyCredential
}