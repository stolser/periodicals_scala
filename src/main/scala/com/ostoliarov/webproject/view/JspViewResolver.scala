package com.ostoliarov.webproject.view

/**
  * Created by Oleg Stoliarov on 10/15/18.
  * Contains methods for converting logical names into paths to concrete files on the server.
  */
object JspViewResolver extends ViewResolver {
	private val JSP_PRIVATE_RESOURCE_PATH_PATTERN = "/WEB-INF/backend/%s.jsp"
	private val JSP_PUBLIC_RESOURCE_PATH_PATTERN = "/%s.jsp"

	override def resolvePrivateViewName(viewName: String): String =
		JSP_PRIVATE_RESOURCE_PATH_PATTERN.format(viewName)

	override def resolvePublicViewName(viewName: String): String =
		JSP_PUBLIC_RESOURCE_PATH_PATTERN.format(viewName)
}
