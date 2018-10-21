package com.stolser.javatraining.webproject.model.entity.user

import java.util.Date

import scala.beans.BeanProperty
import scala.collection.mutable

/**
  * Created by Oleg Stoliarov on 10/20/18.
  *
  * @param status Only { @code active} users can sign into the system.
  * @param roles  Roles define specific system functionality available to a user.
  */
case class User(@BeanProperty id: Long = 0,
				@BeanProperty userName: String = "",
				@BeanProperty firstName: String = "",
				@BeanProperty lastName: String = "",
				@BeanProperty birthday: Date = new Date(),
				@BeanProperty email: String = "",
				@BeanProperty address: String = "",
				@BeanProperty status: UserStatus.Value = UserStatus.BLOCKED,
				@BeanProperty var roles: mutable.Set[UserRole.Value] = mutable.Set()) {

	override def toString: String = s"User{id=$id, userName='%s', firstName='%s', lastName='%s', " +
		"birthDate=%s, email='%s', address='%s', status=%s, roles=%s}"

	def hasRole(role: UserRole.Value): Boolean =
		roles contains role

	def hasRole(role: String): Boolean =
		hasRole(UserRole.withName(role.toUpperCase))

	def getRolesAsJavaCollection: java.util.Set[UserRole.Value] = {
		import scala.collection.JavaConverters._

		roles.asJava
	}
}

object UserStatus extends Enumeration {
	val ACTIVE, BLOCKED = Value
}

object UserRole extends Enumeration {
	val ADMIN, SUBSCRIBER = Value
}

object User {
	private[this] val emptyUser = new User()

	def apply: User = emptyUser
}