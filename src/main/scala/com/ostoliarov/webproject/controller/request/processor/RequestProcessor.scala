package com.ostoliarov.webproject.controller.request.processor

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

/**
  * Created by Oleg Stoliarov on 10/10/18.
  * Together with concrete implementations (processors) implements the Command Design pattern.
  */
trait RequestProcessor {
	/**
	  * Processes a current http request. Can update session or request attributes, analyse request
	  * parameters, generate frontend messages.
	  *
	  * @return a basic view name of the page where this request should be forwarded
	  *         or { @code empty object} if a request was redirected to another uri and it does not
	  *         require to be forwarded.
	  */
	def process(request: HttpServletRequest, response: HttpServletResponse): ResourceRequest
}

abstract class RequestProcessorWrapper // was used to implement trait RequestProcessor from Java code;
	extends RequestProcessor {}
