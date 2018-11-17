package com.stolser.javatraining.webproject

import org.scalamock.scalatest.MockFactory
import org.scalatest._

/**
	* Created by Oleg Stoliarov on 10/21/18.
	*/
abstract class FlatSpecScalaMockBase extends FlatSpec
	with BeforeAndAfter
	with Matchers
	with MockFactory
