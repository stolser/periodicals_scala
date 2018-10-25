package com.stolser.javatraining.webproject.model.entity.user

import java.util.Date

import com.google.common.base.Preconditions.checkNotNull

import scala.beans.BeanProperty

/**
  * Created by Oleg Stoliarov on 10/20/18.
  *
  * @param status Only { @code active} users can sign into the system.
  * @param roles  Roles define specific system functionality available to a user.
  */
case class User(@BeanProperty id: Long = 0,
				@BeanProperty userName: String = "",
				@BeanProperty firstName: Option[String] = None,
				@BeanProperty lastName: Option[String] = None,
				@BeanProperty birthday: Option[Date] = None,
				@BeanProperty email: String = "",
				@BeanProperty address: Option[String] = None,
				@BeanProperty status: UserStatus.Value = UserStatus.BLOCKED,
				@BeanProperty var roles: Set[UserRole.Value] = Set()) {

	checkNotNull(userName)
	checkNotNull(email)
	checkNotNull(status)
	checkNotNull(roles, "Use an empty set instead.": Any)

	override def toString: String = s"User{id=$id, userName='%s', firstName='%s', lastName='%s', " +
		"birthDate=%s, email='%s', address='%s', status=%s, roles=%s}"

	def hasRole(role: UserRole.Value): Boolean =
		roles contains role

	def hasRole(role: String): Boolean =
		hasRole(UserRole.withName(role.toUpperCase))

	def getFirstNameAsString: String = firstName.getOrElse("") // used by JSP tags;

	def getLastNameAsString: String = lastName.getOrElse("") // used by JSP tags;

	def getBirthdayAsDate: Date = birthday.orNull // used by JSP tags;

	def getAddressAsString: String = address.getOrElse("") // used by JSP tags;

	def getRolesAsJavaCollection: java.util.Set[UserRole.Value] = { // used by JSP tags;
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