package com.stolser.javatraining.webproject.controller.encoding

import com.stolser.javatraining.webproject.controller.ApplicationResources
import javax.servlet._

/**
  * Allows entering on the frontend and saving cyrillic symbols in the system.
  */
class CharsetEncoder extends Filter {
	override def init(filterConfig: FilterConfig): Unit = println("Hello from CharsetEncoder#init()")

	override def doFilter(request: ServletRequest, response: ServletResponse, filterChain: FilterChain): Unit = {
		request.setCharacterEncoding(ApplicationResources.CHARACTER_ENCODING)
		filterChain.doFilter(request, response)
	}

	override def destroy(): Unit = println("Hello from CharsetEncoder#destroy()")
}