package com.ostoliarov.webproject.view.jsp.tag

import java.io.IOException
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.Temporal
import java.util.{Date => JavaDate}

import javax.servlet.jsp.tagext.{Tag, TagSupport}
import javax.servlet.jsp.{JspException, JspTagException, PageContext}
import org.apache.taglibs.standard.tag.common.core.Util

import scala.beans.BeanProperty

/**
	* Created by Oleg Stoliarov on 10/15/18.
	* Displays formatted date.
	*/
class FormatDatetimeTag extends TagSupport() {
	@BeanProperty
	var pattern: String = "dd.MM.YYYY HH:mm:ss"

	private var _value: Option[Temporal] = None
	private var `var`: Option[String] = None
	private var _scope: Int = PageContext.PAGE_SCOPE

	def setValue(value: Temporal): Unit = _value = Option(value)

	def getValue: Temporal = _value.orNull

	def setVar(`var`: String): Unit = this.`var` = Option(`var`)

	def setScope(scope: String): Unit = this._scope = Util.getScope(scope)

	@throws[JspException]
	override def doEndTag(): Int =
		_value match {
			case None =>
				if (`var`.isDefined) pageContext.removeAttribute(`var`.get, _scope)
				Tag.EVAL_PAGE

			case Some(someValue) =>
				`var` match {
					case Some(someVar) => pageContext.setAttribute(someVar, formattedDate(someValue), _scope)
					case None =>
						tryToPrintFormattedDate {
							pageContext.getOut.print(formattedDate(someValue))
						}
				}

				Tag.EVAL_PAGE
		}

	private def formattedDate(value: Temporal) =
		new SimpleDateFormat(pattern).format(JavaDate.from(Instant.from(value)))

	private def tryToPrintFormattedDate(block: => Unit): Unit =
		try
			block
		catch {
			case ioe: IOException =>
				throw new JspTagException(ioe.toString, ioe)
		}
}
