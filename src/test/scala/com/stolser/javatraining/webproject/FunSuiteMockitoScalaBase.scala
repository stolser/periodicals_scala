package com.stolser.javatraining.webproject

import org.mockito.{ArgumentMatchersSugar, MockitoSugar}
import org.scalatest._

/**
	* Created by Oleg Stoliarov on 10/21/18.
	*/
abstract class FunSuiteMockitoScalaBase extends FunSuite
	with BeforeAndAfter
	with Matchers
	with MockitoSugar
	with ArgumentMatchersSugar