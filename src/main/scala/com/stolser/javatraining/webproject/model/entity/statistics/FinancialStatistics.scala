package com.stolser.javatraining.webproject.model.entity.statistics

import scala.beans.BeanProperty

/**
  * Created by Oleg Stoliarov on 10/19/18.
  *
  * @param totalInvoiceSum The sum of all invoices that were created regardless whether they have been paid or not.
  * @param paidInvoiceSum  The sum of all invoices that have been paid.
  */
case class FinancialStatistics(@BeanProperty totalInvoiceSum: Long,
							   @BeanProperty paidInvoiceSum: Long) {}