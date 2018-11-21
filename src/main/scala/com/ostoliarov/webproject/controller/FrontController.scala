package com.ostoliarov.webproject.controller

import java.io.IOException

import com.ostoliarov.webproject.controller.request.processor.{DispatchException, RequestProviderImpl}
import com.ostoliarov.webproject.controller.utils.HttpUtils
import com.ostoliarov.webproject.view.JspViewResolver
import javax.servlet.ServletException
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}
import org.slf4j.LoggerFactory

/**
	* Created by Oleg Stoliarov on 10/13/18.
	* Implementation of the Front Controller pattern.
	*/
class FrontController extends HttpServlet {
	private val LOGGER = LoggerFactory.getLogger(classOf[FrontController])
	private val DISPATCHING_TO_THE_VIEW_NAME = "Dispatching to the viewName = '%s'."
	private val INCORRECT_THE_DISPATCH_TYPE = "Incorrect the dispatch type of the abstractViewName: %s"
	private val requestProvider = RequestProviderImpl
	private val viewResolver = JspViewResolver

	@throws[ServletException]
	@throws[IOException]
	override def doGet(request: HttpServletRequest, response: HttpServletResponse): Unit =
		processRequest(request, response)

	@throws[ServletException]
	@throws[IOException]
	override protected def doPost(request: HttpServletRequest, response: HttpServletResponse): Unit =
		processRequest(request, response)

	@throws[ServletException]
	@throws[IOException]
	private def processRequest(request: HttpServletRequest, response: HttpServletResponse): Unit =
		tryToProcessRequestAndDispatchResponse(request, response) {
			val abstractViewName = requestProvider.requestProcessor(request).process(request, response)
			dispatch(abstractViewName, request, response)
		}

	private def tryToProcessRequestAndDispatchResponse(request: HttpServletRequest, response: HttpServletResponse)
																										(block: => Unit): Unit =
		try {
			block
		} catch {
			case e: RuntimeException =>
				logExceptionAndRedirectToErrorPage(request, response, e)
		}

	private def dispatch(abstractViewName: String,
											 request: HttpServletRequest,
											 response: HttpServletResponse): Unit = {
		val viewNameParts = abstractViewName.split(":")
		val dispatchType = viewNameParts(0)
		val viewName = viewNameParts(1)

		dispatchType match {
			case "forward" =>
				sendForward(request, response, viewName)
			case "redirect" =>
				HttpUtils.tryToSendRedirect(request, response, viewName)
			case "noAction" => ()
			case _ =>
				throw new IllegalArgumentException(INCORRECT_THE_DISPATCH_TYPE.format(abstractViewName))
		}
	}

	def sendForward(request: HttpServletRequest, response: HttpServletResponse, viewName: String): Unit =
		try {
			val dispatcher = request.getRequestDispatcher(viewResolver.resolvePrivateViewName(viewName))
			dispatcher.forward(request, response)
		} catch {
			case e@(_: ServletException | _: IOException) =>
				throw DispatchException(DISPATCHING_TO_THE_VIEW_NAME.format(viewName), e)
		}

	private def logExceptionAndRedirectToErrorPage(request: HttpServletRequest,
																								 response: HttpServletResponse,
																								 e: RuntimeException): Unit = {
		LOGGER.error(s"User id = ${HttpUtils.userIdFromSession(request)}. requestURI = ${request.getRequestURI}", e)
		HttpUtils.tryToSendRedirect(request, response, viewResolver.resolvePublicViewName(HttpUtils.errorViewName(e)))
	}
}
