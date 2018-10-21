package com.stolser.javatraining.webproject.model.entity.user

import scala.beans.BeanProperty

/**
  * Created by Oleg Stoliarov on 10/20/18.
  */
case class Credential(@BeanProperty id: Long = 0,
					  @BeanProperty userName: String = "",
					  @BeanProperty passwordHash: String = "",
					  @BeanProperty var userId: Long = 0) {

	override def toString: String = s"Credentials{id=$id, userName='$userName', userId='$userId'}"
}

object Credential {
	private[this] val emptyCredential = new Credential()

	def apply: Credential = emptyCredential
}