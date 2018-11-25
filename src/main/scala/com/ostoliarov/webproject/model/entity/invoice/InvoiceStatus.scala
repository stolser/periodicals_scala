package com.ostoliarov.webproject.model.entity.invoice

/**
	* Created by Oleg Stoliarov on 11/25/18.
	*/
object InvoiceStatus extends Enumeration {
	type InvoiceStatus = Value
	val NEW, PAID = Value
}
