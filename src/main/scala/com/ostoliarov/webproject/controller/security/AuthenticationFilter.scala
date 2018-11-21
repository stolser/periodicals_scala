package com.ostoliarov.webproject.controller.security

import com.ostoliarov.webproject.controller.ApplicationResources.{LOGIN_PAGE, ORIGINAL_URI_ATTR_NAME, SIGN_OUT_URI}
import com.ostoliarov.webproject.controller.utils.{HttpUtils, HttpUtilsTrait}
import com.ostoliarov.webproject.model.entity.user.{User, UserStatus}
import javax.servlet._
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

/**
	* Created by Oleg Stoliarov on 10/12/18.
	* Makes sure that this request comes from a signed in and active user and the session has not expired.
	* Otherwise it redirects to the Sing-in page.
	*/
class AuthenticationFilter extends Filter {
	private[security] val httpUtils: HttpUtilsTrait = HttpUtils
	private val unProtectedUris = Array(
		"/backend/signIn",
		"/backend/signUp",
		"/backend/validation",
	)

	override def init(filterConfig: FilterConfig): Unit = {}

	override def doFilter(servletRequest: ServletRequest,
												servletResponse: ServletResponse,
												filterChain: FilterChain): Unit = {
		val request = servletRequest.asInstanceOf[HttpServletRequest]
		val response = servletResponse.asInstanceOf[HttpServletResponse]

		if (requestNotRequiresAuthentication(request)) {
			filterChain.doFilter(servletRequest, servletResponse)
		} else {
			httpUtils.currentUserFromFromDb(request) match {
				case None =>
					request.getSession.setAttribute(ORIGINAL_URI_ATTR_NAME, request.getRequestURI)
					response.sendRedirect(LOGIN_PAGE)
				case Some(user) =>
					if (isUserNotActive(user))
						response.sendRedirect(SIGN_OUT_URI)
					else
						filterChain.doFilter(servletRequest, servletResponse)
			}
		}
	}

	private def requestNotRequiresAuthentication(request: HttpServletRequest) =
		unProtectedUris contains request.getRequestURI

	private def isUserNotActive(currentUser: User) =
		UserStatus.ACTIVE != currentUser.status

	override def destroy(): Unit = {}
}
