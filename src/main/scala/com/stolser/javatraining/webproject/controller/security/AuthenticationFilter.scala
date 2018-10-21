package com.stolser.javatraining.webproject.controller.security

import java.util
import java.util.Objects.isNull
import java.util.{Arrays, List}

import com.stolser.javatraining.webproject.controller.ApplicationResources.{LOGIN_PAGE, ORIGINAL_URI_ATTR_NAME, SIGN_OUT_URI}
import com.stolser.javatraining.webproject.controller.utils.HttpUtils
import com.stolser.javatraining.webproject.model.entity.user.{User, UserStatus}
import javax.servlet._
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

/**
  * Created by Oleg Stoliarov on 10/12/18.
  * Makes sure that this request comes from a signed in and active user and the session has not expired.
  * Otherwise it redirects to the Log in page.
  */
class AuthenticationFilter extends Filter {
	private val unProtectedUris = util.Arrays.asList("/backend/signIn", "/backend/signUp", "/backend/validation")

	override def init(filterConfig: FilterConfig): Unit = {}

	override def doFilter(servletRequest: ServletRequest,
						  servletResponse: ServletResponse,
						  filterChain: FilterChain): Unit = {
		val request = servletRequest.asInstanceOf[HttpServletRequest]
		val response = servletResponse.asInstanceOf[HttpServletResponse]

		if (requestNotRequiresAuthentication(request)) {
			filterChain.doFilter(servletRequest, servletResponse)
			return
		}

		val requestUri = request.getRequestURI
		val currentUser = HttpUtils.getCurrentUserFromFromDb(request)

		if (isNull(currentUser)) {
			request.getSession.setAttribute(ORIGINAL_URI_ATTR_NAME, requestUri)
			response.sendRedirect(LOGIN_PAGE)
		}
		else if (isUserNotActive(currentUser)) response.sendRedirect(SIGN_OUT_URI)
		else filterChain.doFilter(servletRequest, servletResponse)
	}

	private def requestNotRequiresAuthentication(request: HttpServletRequest) =
		unProtectedUris.contains(request.getRequestURI)

	private def isUserNotActive(currentUser: User) =
		UserStatus.ACTIVE != currentUser.getStatus


	override def destroy(): Unit = {}
}
