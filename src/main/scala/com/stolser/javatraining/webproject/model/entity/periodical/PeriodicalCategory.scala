package com.stolser.javatraining.webproject.model.entity.periodical

import com.stolser.javatraining.webproject.controller.ApplicationResources._
import enumeratum.{Enum, EnumEntry}

import scala.beans.BeanProperty
import scala.collection.immutable

/**
	* Created by Oleg Stoliarov on 10/19/18.
	*/
sealed abstract class PeriodicalCategory(_messageKey: String) extends EnumEntry {
	@BeanProperty val messageKey: String = _messageKey
}

object PeriodicalCategory extends Enum[PeriodicalCategory] {
	val values: immutable.IndexedSeq[PeriodicalCategory] = findValues

	case object NEWS extends PeriodicalCategory(MSG_KEY_CATEGORY_NEWS)

	case object NATURE extends PeriodicalCategory(MSG_KEY_CATEGORY_NATURE)

	case object FITNESS extends PeriodicalCategory(MSG_KEY_CATEGORY_FITNESS)

	case object BUSINESS extends PeriodicalCategory(MSG_KEY_CATEGORY_BUSINESS)

	case object SPORTS extends PeriodicalCategory(MSG_KEY_CATEGORY_SPORTS)

	case object SCIENCE_AND_ENGINEERING extends PeriodicalCategory(MSG_KEY_CATEGORY_SCIENCE_AND_ENGINEERING)

	case object TRAVELLING extends PeriodicalCategory(MSG_KEY_CATEGORY_TRAVELLING)

}