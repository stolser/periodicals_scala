package com.ostoliarov.webproject.controller.security

import com.ostoliarov.webproject.controller.ApplicationResources._
import com.ostoliarov.webproject.controller.security.AuthorizationFilter.ACCESS_DENIED_FOR_USER
import com.ostoliarov.webproject.model.entity.user.User
import com.ostoliarov.webproject.view.JspViewResolver
import javax.servlet._
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.slf4j.LoggerFactory

/**
	* Created by Oleg Stoliarov on 10/12/18.
	* Checks whether a current user has enough permissions to get a requested resource or perform an operation.
	*/
object AuthorizationFilter {
	val ACCESS_DENIED_FOR_USER = "Access denied for user '%s' to '%s'!!!"
}

class AuthorizationFilter extends Filter {
	private val LOGGER = LoggerFactory.getLogger(classOf[AuthorizationFilter])
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

		if (isRequestAuthorized(request))
			filterChain.doFilter(request, response)
		else {
			logErrorAndRedirectToErrorPage(request, response)
		}
	}

	private def logErrorAndRedirectToErrorPage(request: HttpServletRequest,
																						 response: HttpServletResponse): Unit = {
		val errorMessage = ACCESS_DENIED_FOR_USER.format(getUserNameFromSession(request), request.getRequestURI)
		LOGGER.error(errorMessage)
		request.getSession.setAttribute(ERROR_MESSAGE_ATTR_NAME, errorMessage)
		response.sendRedirect(viewResolver.resolvePublicViewName(ACCESS_DENIED_PAGE_VIEW_NAME))
	}

	private def getUserNameFromSession(request: HttpServletRequest) =
		request.getSession.getAttribute(CURRENT_USER_ATTR_NAME).asInstanceOf[User].userName

	private def isRequestAuthorized(request: HttpServletRequest) = Authorization.checkPermissions(request)

	override def destroy(): Unit = {}
}
