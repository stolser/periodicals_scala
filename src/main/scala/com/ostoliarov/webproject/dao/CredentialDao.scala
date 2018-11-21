package com.ostoliarov.webproject.dao

import com.ostoliarov.webproject.model.entity.user.Credential

/**
	* Created by Oleg Stoliarov on 10/13/18.
	*/
trait CredentialDao {
	def findCredentialByUserName(userName: String): Option[Credential]

	def createNew(credential: Credential): Boolean
}
