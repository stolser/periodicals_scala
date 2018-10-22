package com.stolser.javatraining.webproject.model.entity.periodical

import com.google.common.base.Preconditions
import com.google.common.base.Preconditions._

import scala.beans.BeanProperty

/**
  * Created by Oleg Stoliarov on 10/19/18.
  */
case class Periodical(@BeanProperty id: Long = 0,
					  @BeanProperty name: String = "",
					  @BeanProperty category: PeriodicalCategory = PeriodicalCategory.NEWS,
					  @BeanProperty publisher: String = "",
					  @BeanProperty description: String = "",
					  @BeanProperty oneMonthCost: Long = 0,
					  @BeanProperty status: PeriodicalStatus.Value = PeriodicalStatus.ACTIVE) {

	checkNotNull(name)
	checkNotNull(category)
	checkNotNull(publisher)
	checkNotNull(description)
	checkNotNull(status)

	override def toString: String = {
		val description =
			if (this.description.length <= 15)
				this.description
			else
				this.description.substring(0, 15)

		s"Periodical{id=$id, name='$name', category='$category', publisher='$publisher', " +
			s"description='$description', oneMonthCost='$oneMonthCost', status='$status'}"
	}
}

object PeriodicalStatus extends Enumeration {
	val ACTIVE, INACTIVE, DISCARDED = Value
}

object PeriodicalOperationType extends Enumeration {
	val CREATE, UPDATE = Value
}

object Periodical {
	private[this] val emptyPeriodical = new Periodical()

	def apply: Periodical = emptyPeriodical
}