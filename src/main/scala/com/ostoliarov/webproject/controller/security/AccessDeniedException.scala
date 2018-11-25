package com.ostoliarov.webproject.controller.security

/**
  * Created by Oleg Stoliarov on 10/12/18.
  */
case class AccessDeniedException private(message: String) extends RuntimeException(message)
