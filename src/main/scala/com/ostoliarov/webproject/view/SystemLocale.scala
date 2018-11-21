package com.ostoliarov.webproject.view

import java.util.Locale

import enumeratum._

import scala.collection.immutable

/**
  * Created by Oleg Stoliarov on 10/15/18.
  */
sealed abstract class SystemLocale(val locale: Locale) extends EnumEntry

object SystemLocale extends Enum[SystemLocale] {
	val values: immutable.IndexedSeq[SystemLocale] = findValues

	case object EN_EN extends SystemLocale(Locale.ENGLISH)
	case object RU_RU extends SystemLocale(new Locale("ru", "RU"))
	case object UK_UA extends SystemLocale(new Locale("uk", "UA"))
}