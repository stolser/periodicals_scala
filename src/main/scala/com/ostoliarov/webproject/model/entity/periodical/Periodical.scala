package com.ostoliarov.webproject.model.entity.periodical

import com.google.common.base.Preconditions._
import com.ostoliarov.webproject.model.entity.periodical.PeriodicalStatus.PeriodicalStatus

import scala.beans.BeanProperty

/**
	* Created by Oleg Stoliarov on 10/19/18.
	*/
case class Periodical private(@BeanProperty id: Long = 0,
															@BeanProperty name: String = "",
															@BeanProperty category: PeriodicalCategory = PeriodicalCategory.NEWS,
															@BeanProperty publisher: String = "",
															@BeanProperty description: Option[String] = None,
															@BeanProperty oneMonthCost: Long = 0,
															@BeanProperty status: PeriodicalStatus = PeriodicalStatus.ACTIVE) {

	require(id >= 0)
	require(oneMonthCost >= 0)
	checkNotNull(name)
	checkNotNull(category)
	checkNotNull(publisher)
	checkNotNull(status)

	override def toString: String = {
		def getDescriptionWithLimitedLength(d: String) =
			if (d.length <= 15) d
			else d.substring(0, 15)

		val description = this.description match {
			case Some(d) => getDescriptionWithLimitedLength(d)
			case None => ""
		}

		s"Periodical{id=$id, name='$name', category='$category', publisher='$publisher', " +
			s"description='$description', oneMonthCost='$oneMonthCost', status='$status'}"
	}

	@BeanProperty val descriptionAsString: String = description.getOrElse("") // used by JSP tags;
}

object Periodical {
	private[this] val emptyPeriodical = new Periodical()

	def apply: Periodical = emptyPeriodical
}