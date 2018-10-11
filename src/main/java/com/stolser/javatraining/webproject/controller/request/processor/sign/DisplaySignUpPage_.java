package com.stolser.javatraining.webproject.controller.request.processor.sign;

import com.stolser.javatraining.webproject.controller.request.processor.RequestProcessor;
import com.stolser.javatraining.webproject.model.entity.user.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.stolser.javatraining.webproject.controller.ApplicationResources.SIGN_UP_PAGE_VIEW_NAME;

public class DisplaySignUpPage_ implements RequestProcessor {
    private DisplaySignUpPage_() {}

    private static class InstanceHolder {
        private static final DisplaySignUpPage_ INSTANCE = new DisplaySignUpPage_();
    }

    public static DisplaySignUpPage_ getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public String process(HttpServletRequest request, HttpServletResponse response) {
        request.setAttribute("roles", User.Role.values());
        return FORWARD + SIGN_UP_PAGE_VIEW_NAME;
    }
}
