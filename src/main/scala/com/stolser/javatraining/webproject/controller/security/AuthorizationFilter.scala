package com.stolser.javatraining.webproject.controller.security

import com.stolser.javatraining.webproject.controller.ApplicationResources.{ACCESS_DENIED_PAGE_VIEW_NAME, CURRENT_USER_ATTR_NAME}
import com.stolser.javatraining.webproject.model.entity.user.User
import com.stolser.javatraining.webproject.view.JspViewResolver
import javax.servlet._
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.slf4j.LoggerFactory

/**
  * Created by Oleg Stoliarov on 10/12/18.
  * Checks whether a current user has enough permissions to get a requested resource or perform an operation.
  */
class AuthorizationFilter extends Filter {
	private val LOGGER = LoggerFactory.getLogger(classOf[AuthorizationFilter])
	private val ACCESS_DENIED_FOR_USER = "Access denied for user '%s' to '%s'!!!%n"
	private val viewResolver = JspViewResolver

	override def init(filterConfig: FilterConfig): Unit = {}

	/**
	  * Proceeds to the next resource if a current user has enough permissions, and
	  * redirects to 'access denied page' otherwise.
	  */
	override def doFilter(servletRequest: ServletRequest,
						  servletResponse: ServletResponse,
						  filterChain: FilterChain): Unit = {
		val request = servletRequest.asInstanceOf[HttpServletRequest]
		val response = servletResponse.asInstanceOf[HttpServletResponse]

		if (isRequestAuthorized(request)) filterChain.doFilter(request, response)
		else {
			logAccessDeniedMessage(request)
			response.sendRedirect(viewResolver.resolvePublicViewName(ACCESS_DENIED_PAGE_VIEW_NAME))
		}
	}

	private def logAccessDeniedMessage(request: HttpServletRequest): Unit = {
		LOGGER.error(String.format(ACCESS_DENIED_FOR_USER, getUserNameFromSession(request), request.getRequestURI))
	}

	private def getUserNameFromSession(request: HttpServletRequest) =
		request.getSession.getAttribute(CURRENT_USER_ATTR_NAME).asInstanceOf[User].userName

	private def isRequestAuthorized(request: HttpServletRequest) =
		Authorization.checkPermissions(request)

	override def destroy(): Unit = {}
}
