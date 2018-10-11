package com.stolser.javatraining.webproject.controller.request.processor

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

/**
  * Created by Oleg Stoliarov on 10/10/18.
  * Together with concrete implementations (processors) implements the Command Design pattern.
  */
trait RequestProcessor {
	val FORWARD = "forward:"
	val REDIRECT = "redirect:"
	val NO_ACTION = "noAction:noUri"

	/**
	  * Processes a current http request. Can update session or request attributes, analyse request
	  * parameters, generate frontend messages.
	  *
	  * @return a basic view name of the page where this request should be forwarded
	  *         or { @code empty object} if a request was redirected to another uri and it does not
	  *         require to be forwarded.
	  */
	def process(request: HttpServletRequest, response: HttpServletResponse): String
}

abstract class RequestProcessorWrapper extends RequestProcessor {}