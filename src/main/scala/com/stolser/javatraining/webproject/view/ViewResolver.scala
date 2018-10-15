package com.stolser.javatraining.webproject.view

/**
  * Created by Oleg Stoliarov on 10/15/18.
  */
trait ViewResolver {
	/**
	  * Returns a path to a file that will generate html content of a private page to be sent to the client.
	  *
	  * @param viewName a logical name of a private resource access to which
	  *                 requires authentication and authorization
	  */
	def resolvePrivateViewName(viewName: String): String

	/**
	  * Returns a path to a file that will generate html content of a public page to be sent to the client.
	  *
	  * @param viewName a logical name of a public resource access to which does not require any authentication
	  */
	def resolvePublicViewName(viewName: String): String
}
