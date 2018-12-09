package com.ostoliarov.webproject.controller

import java.io.IOException

import com.ostoliarov.eventsourcing.EventSourcingApp
import com.ostoliarov.webproject.controller.ApplicationResources.ERROR_MESSAGE_ATTR_NAME
import com.ostoliarov.webproject.controller.request.processor.DispatchType.{DispatchType => _, _}
import com.ostoliarov.webproject.controller.request.processor._
import com.ostoliarov.webproject.controller.utils.HttpUtils._
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
	private val DISPATCHING_TO_VIEW_NAME = "Dispatching to the viewName = '%s'."
	private val INCORRECT_DISPATCH_TYPE = "Incorrect the dispatch type of the resource request: %s"
	private val requestProvider = RequestProviderImpl
	private val viewResolver = JspViewResolver

	override def destroy(): Unit = {
		EventSourcingApp.shutdown()
		Thread.sleep(3000)
	}

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
			removeTempAttributesFromSession(request)

			val resourceRequest = requestProvider.requestProcessor(request).process(request, response)
			dispatch(resourceRequest, request, response)
		}

	private def removeTempAttributesFromSession(request: HttpServletRequest): Unit =
		request.getSession.removeAttribute(ERROR_MESSAGE_ATTR_NAME)

	private def tryToProcessRequestAndDispatchResponse(request: HttpServletRequest, response: HttpServletResponse)
																										(block: => Unit): Unit =
		try {
			block
		} catch {
			case e: RuntimeException =>
				logExceptionAndRedirectToErrorPage(request, response, e)
		}

	private def dispatch(resourceRequest: ResourceRequest,
											 request: HttpServletRequest,
											 response: HttpServletResponse): Unit =
		resourceRequest match {
			case ResourceRequest(FORWARD, AbstractViewName(viewName)) =>
				sendForward(request, response, viewName)
			case ResourceRequest(REDIRECT, AbstractViewName(viewName)) =>
				tryToSendRedirect(request, response, viewName)
			case ResourceRequest(NO_ACTION, _) => ()
			case _ =>
				throw new IllegalArgumentException(INCORRECT_DISPATCH_TYPE.format(resourceRequest))
		}

	def sendForward(request: HttpServletRequest, response: HttpServletResponse, viewName: String): Unit =
		try {
			val dispatcher = request.getRequestDispatcher(viewResolver.resolvePrivateViewName(viewName))
			dispatcher.forward(request, response)
		} catch {
			case e@(_: ServletException | _: IOException) =>
				throw DispatchException(DISPATCHING_TO_VIEW_NAME.format(viewName), e)
		}

	private def logExceptionAndRedirectToErrorPage(request: HttpServletRequest,
																								 response: HttpServletResponse,
																								 e: RuntimeException): Unit = {
		LOGGER.error(s"Exception during requesting URI = ${request.getRequestURI} " +
			s"by a user with id = ${userIdFromSession(request)} ", e)

		errorViewNameAndOriginalMessage(e) match {
			case (ResourceRequest(REDIRECT, AbstractViewName(viewName)), errorMessage) =>
				// add it to the session since we redirect to the error page
				// and request attributes won't survive redirect;
				request.getSession.setAttribute(ERROR_MESSAGE_ATTR_NAME, errorMessage)

				tryToSendRedirect(request, response, viewResolver.resolvePublicViewName(viewName))
		}
	}
}
