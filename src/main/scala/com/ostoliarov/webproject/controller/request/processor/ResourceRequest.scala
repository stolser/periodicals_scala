package com.ostoliarov.webproject.controller.request.processor

import com.ostoliarov.webproject.controller.request.processor.DispatchType.DispatchType

/**
	* Created by Oleg Stoliarov on 11/22/18.
	*/
case class ResourceRequest(dispatchType: DispatchType,
													 abstractViewName: AbstractViewName)

object DispatchType extends Enumeration {
	type DispatchType = Value
	val FORWARD, REDIRECT, NO_ACTION = Value
}

case class AbstractViewName(viewName: String)
