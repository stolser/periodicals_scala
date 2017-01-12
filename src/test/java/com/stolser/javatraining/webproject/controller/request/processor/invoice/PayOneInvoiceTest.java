package com.stolser.javatraining.webproject.controller.request.processor.invoice;

import com.stolser.javatraining.webproject.controller.TestResources;
import com.stolser.javatraining.webproject.controller.form.validator.front.message.FrontMessageFactory;
import com.stolser.javatraining.webproject.model.entity.invoice.Invoice;
import com.stolser.javatraining.webproject.model.entity.periodical.Periodical;
import com.stolser.javatraining.webproject.model.entity.user.User;
import com.stolser.javatraining.webproject.service.InvoiceService;
import com.stolser.javatraining.webproject.service.PeriodicalService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static com.stolser.javatraining.webproject.controller.ApplicationResources.CURRENT_USER_ACCOUNT_URI;
import static com.stolser.javatraining.webproject.controller.ApplicationResources.CURRENT_USER_ATTR_NAME;
import static org.mockito.Mockito.*;

public class PayOneInvoiceTest {
    private static final int PERIODICAL_ID = 7;
    @Mock
    private InvoiceService invoiceService;
    @Mock
    private PeriodicalService periodicalService;

    @Mock
    private FrontMessageFactory messageFactory;

    @InjectMocks
    private PayOneInvoice payOneInvoice;
    @Mock
    private HttpSession session;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    private Invoice invoice;
    private User user;
    private Periodical periodical;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        periodical = new Periodical();
        periodical.setId(PERIODICAL_ID);
        periodical.setStatus(Periodical.Status.ACTIVE);

        Invoice.Builder invoiceBuilder = new Invoice.Builder();
        invoiceBuilder.setId(10)
                .setStatus(Invoice.Status.NEW)
                .setPeriodical(periodical);
        invoice = invoiceBuilder.build();

        user = new User();
        user.setId(2);

//        session = mock(HttpSession.class);
        when(session.getAttribute(CURRENT_USER_ATTR_NAME)).thenReturn(user);


//        request = mock(HttpServletRequest.class);
        when(request.getSession()).thenReturn(session);
        when(request.getRequestURI()).thenReturn(TestResources.USER_2_INVOICE_10_PAYMENT);


//        response = mock(HttpServletResponse.class);

//        invoiceService = mock(InvoiceService.class);
        when(invoiceService.findOneById(10)).thenReturn(invoice);
        when(invoiceService.payInvoice(invoice)).thenReturn(true);

//        periodicalService = mock(PeriodicalService.class);
        when(periodicalService.findOneById(PERIODICAL_ID)).thenReturn(periodical);

    }

    @Test
    @Ignore
    public void getViewName_Should_PayInvoiceSuccessfully() throws Exception {
        System.out.println("invoiceService in the test = " + invoiceService.hashCode());

        payOneInvoice.process(request, response);

        verify(invoiceService, times(1)).findOneById(10);
        verify(request, times(2)).getRequestURI();
        verify(request, times(2)).getSession();
        verify(response, times(1)).sendRedirect(CURRENT_USER_ACCOUNT_URI);
        verify(periodicalService, times(1)).findOneById(PERIODICAL_ID);
        verify(invoiceService, times(1)).payInvoice(invoice);

        verify(messageFactory, times(1)).getInfo(anyString());
        verify(messageFactory, times(1)).getSuccess(anyString());
    }
}