package com.stolser.javatraining.webproject.dao

import com.stolser.javatraining.webproject.model.entity.user.Credential

/**
  * Created by Oleg Stoliarov on 10/13/18.
  */
trait CredentialDao {
	def findCredentialByUserName(userName: String): Credential

	def createNew(credential: Credential): Boolean
}
