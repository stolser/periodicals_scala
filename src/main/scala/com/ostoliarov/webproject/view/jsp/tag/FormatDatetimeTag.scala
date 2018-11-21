package com.ostoliarov.webproject.view.jsp.tag

import java.io.IOException
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.Temporal
import java.util.Date
import java.util.Objects.{isNull, nonNull}

import javax.servlet.jsp.tagext.{Tag, TagSupport}
import javax.servlet.jsp.{JspException, JspTagException, PageContext}
import org.apache.taglibs.standard.tag.common.core.Util

import scala.beans.BeanProperty

/**
	* Created by Oleg Stoliarov on 10/15/18.
	* Displays formatted date.
	*/
class FormatDatetimeTag extends TagSupport() {
	@BeanProperty var value: Temporal = _
	@BeanProperty var pattern: String = "dd.MM.YYYY HH:mm:ss"
	private var `var`: String = _
	private var _scope: Int = PageContext.PAGE_SCOPE

	def setVar(`var`: String): Unit =
		this.`var` = `var`

	def setScope(scope: String): Unit =
		this._scope = Util.getScope(scope)

	@throws[JspException]
	override def doEndTag(): Int = {
		if (isNull(value)) {
			if (nonNull(`var`)) pageContext.removeAttribute(`var`, _scope)
			return Tag.EVAL_PAGE
		}

		val instant = Instant.from(value)
		val formatted: String = new SimpleDateFormat(pattern).format(Date.from(instant))

		if (nonNull(`var`))
			pageContext.setAttribute(`var`, formatted, _scope)
		else
			try
				pageContext.getOut.print(formatted)
			catch {
				case ioe: IOException =>
					throw new JspTagException(ioe.toString, ioe)
			}

		Tag.EVAL_PAGE
	}

	override def release(): Unit =
		value = null
}
