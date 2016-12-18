package com.stolser.javatraining.webproject.controller.request_processor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface RequestProcessor {
    /**
     * Processes a current http request. Can update session or request attributes, analyse request
     * parameters, generate frontend messages.
     * @return a basic view name of the page where this request should be forwarded
     * or {@code null} if a request was redirected to another uri.
     */
    String getViewName(HttpServletRequest request, HttpServletResponse response);
}
