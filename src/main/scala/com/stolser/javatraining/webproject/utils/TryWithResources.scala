package com.stolser.javatraining.webproject.utils

import scala.util.control.NonFatal

/**
  * Created by Oleg Stoliarov on 10/14/18.
  * Emulates Java's try-with-resources facility.
  */
object TryWithResources {
	def withResources[R <: AutoCloseable, V](r: => R)
											(block: R => V): V = {
		val resource: R = r
		require(resource != null, "The resource is null")
		var exception: Throwable = null
		try {
			block(resource)						// executing the block of code passed as the second parameter;
		} catch {
			case NonFatal(e) => exception = e
				throw e
		} finally {
			closeAndAddSuppressed(exception, resource)
		}
	}

	private def closeAndAddSuppressed(e: Throwable,
									  resource: AutoCloseable): Unit = {
		if (e != null) {
			try
				resource.close()
			catch {
				case NonFatal(suppressed) => e.addSuppressed(suppressed)
			}
		} else
			resource.close()
	}
}
