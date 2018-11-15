package com.stolser.javatraining.webproject.controller.request.processor

import com.stolser.javatraining.webproject.controller.ApplicationResources.BACKEND_MAIN_PAGE_VIEW_NAME
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

/**
	* Created by Oleg Stoliarov on 10/10/18.
	* Processes a GET request to a backend main page.
	*/
object DisplayBackendHomePage extends RequestProcessor {
	override def process(request: HttpServletRequest,
											 response: HttpServletResponse): String =
		FORWARD + BACKEND_MAIN_PAGE_VIEW_NAME
}
