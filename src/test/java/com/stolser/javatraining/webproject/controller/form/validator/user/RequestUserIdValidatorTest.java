package com.stolser.javatraining.webproject.controller.form.validator.user;

import com.stolser.javatraining.webproject.controller.TestResources;
import com.stolser.javatraining.webproject.model.entity.user.User;
import org.junit.Before;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static com.stolser.javatraining.webproject.controller.ApplicationResources.CURRENT_USER_ATTR_NAME;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequestUserIdValidatorTest {
    private static final int USER_ID = 2;
    private HttpSession session = mock(HttpSession.class);
    private HttpServletRequest request = mock(HttpServletRequest.class);
    private User user = User.apply(USER_ID);

    @Before
    public void setUp() throws Exception {

        session = mock(HttpSession.class);
        when(session.getAttribute(CURRENT_USER_ATTR_NAME())).thenReturn(user);

        when(request.getSession()).thenReturn(session);
        when(request.getRequestURI()).thenReturn(TestResources.USER_2_INVOICE_10_PAYMENT);
    }
}