package com.stolser.javatraining.webproject

import org.scalamock.proxy.ProxyMockFactory
import org.scalamock.scalatest.MockFactory
import org.scalatest._

/**
  * Created by Oleg Stoliarov on 10/21/18.
  */
abstract class FunSuiteFunctionBase extends FunSuite
	with Matchers
	with OptionValues
	with Inside
	with Inspectors
	with BeforeAndAfter
	with MockFactory