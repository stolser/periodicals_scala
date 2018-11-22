package com.ostoliarov.webproject.model.entity.user

import java.util.{Date => JavaDate, Set => JavaSet}

import com.google.common.base.Preconditions.checkNotNull

import scala.beans.BeanProperty
import scala.collection.JavaConverters._

/**
	* Created by Oleg Stoliarov on 10/20/18.
	*
	* @param status Only { @code active} users can sign into the system.
	* @param roles  Roles define specific system functionality available to a user.
	*/
case class User private(@BeanProperty id: Long = 0,
												@BeanProperty userName: String = "",
												@BeanProperty firstName: Option[String] = None,
												@BeanProperty lastName: Option[String] = None,
												@BeanProperty birthday: Option[JavaDate] = None,
												@BeanProperty email: String = "",
												@BeanProperty address: Option[String] = None,
												@BeanProperty status: UserStatus.Value = UserStatus.BLOCKED,
												@BeanProperty var roles: Set[UserRole.Value] = Set()) {

	checkNotNull(userName)
	checkNotNull(email)
	checkNotNull(status)
	checkNotNull(roles, "Use an empty set instead.": Any)

	override def toString: String = s"User{id=$id, userName='$userName', firstName='$firstName', lastName='$lastName', " +
		s"birthday='$birthday', email='$email', address='$address', status='$status', roles='$roles'}"

	def hasRole(role: UserRole.Value): Boolean =
		roles contains role

	def hasRole(role: String): Boolean =
		hasRole(UserRole.withName(role.toUpperCase))

	@BeanProperty val firstNameAsString: String = firstName.getOrElse("") // used by JSP tags;
	@BeanProperty val lastNameAsString: String = lastName.getOrElse("") // used by JSP tags;
	@BeanProperty val birthdayAsDate: JavaDate = birthday.orNull // used by JSP tags;
	@BeanProperty val addressAsString: String = address.getOrElse("") // used by JSP tags;
	@BeanProperty val rolesAsJavaCollection: JavaSet[UserRole.Value] = roles.asJava // used by JSP tags;
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