package com.ostoliarov.webproject

import org.mockito.{ArgumentMatchersSugar, MockitoSugar}
import org.scalatest.{BeforeAndAfter, Matchers, WordSpec}

/**
	* Created by Oleg Stoliarov on 11/25/18.
	*/
abstract class WordSpecWithMockitoScalaBase extends WordSpec
	with BeforeAndAfter
	with Matchers
	with MockitoSugar
	with ArgumentMatchersSugar
