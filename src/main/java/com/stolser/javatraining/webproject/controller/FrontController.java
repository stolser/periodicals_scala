package com.stolser.javatraining.webproject.controller;

import com.stolser.javatraining.webproject.controller.utils.HttpUtils;
import com.stolser.javatraining.webproject.view.ViewResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class FrontController extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(FrontController.class);
    private static final String USER_ID_REQUEST_URI = "User id = {}. requestURI = {}";
    private static RequestProvider requestProvider = RequestProviderImpl.getInstance();

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String viewName;

        try {
            viewName = requestProvider.getRequestProcessor(request).getViewName(request, response);

        } catch (Exception e) {
            LOGGER.error(USER_ID_REQUEST_URI,
                    HttpUtils.getUserIdFromSession(request), request.getRequestURI(), e);

            viewName = ApplicationResources.getErrorViewName(e);
        }

        dispatch(viewName, request, response);
    }

    private void dispatch(String viewName, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (viewName != null) {
            String page = ViewResolver.getPageByViewName(viewName);

//            System.out.println("forwarding to '" + page + "'");

            RequestDispatcher dispatcher = request.getRequestDispatcher(page);
            dispatcher.forward(request, response);
        }
    }
}
