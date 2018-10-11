package com.stolser.javatraining.webproject.controller.request.processor

import javax.servlet.http.HttpServletRequest

/**
  * Created by Oleg Stoliarov on 10/10/18.
  * { @link FrontController} to move each request processing logic into separate classes.
  */
trait RequestProvider {
	/**
	  * Returns a specific request processing implementation of the { @code RequestProcessor} interface.
	  */
	def getRequestProcessor(request: HttpServletRequest): RequestProcessor
}
