package com.stolser.javatraining.webproject.controller;

import com.stolser.javatraining.webproject.controller.command.DisplayAdminPanelMainPage;
import com.stolser.javatraining.webproject.controller.command.RequestProcessor;
import com.stolser.javatraining.webproject.controller.command.invoice.PersistOneInvoice;
import com.stolser.javatraining.webproject.controller.command.periodical.*;
import com.stolser.javatraining.webproject.controller.command.user.DisplayAllUsers;
import com.stolser.javatraining.webproject.controller.command.user.DisplayCurrentUser;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Pattern;

public class RequestProvider {
    private static final Map<String, RequestProcessor> requestMapping = new HashMap<>();

    static {
        requestMapping.put("GET:/adminPanel/?", new DisplayAdminPanelMainPage());
        requestMapping.put("GET:/adminPanel/users/?", new DisplayAllUsers());
        requestMapping.put("GET:/adminPanel/users/currentUser/?", new DisplayCurrentUser());
        requestMapping.put("GET:/adminPanel/periodicals/\\d+", new DisplayOnePeriodical());
        requestMapping.put("GET:/adminPanel/periodicals/?", new DisplayAllPeriodicals());
        requestMapping.put("POST:/adminPanel/periodicals/?", new PersistOnePeriodical());
        requestMapping.put("GET:/adminPanel/periodicals/createNew/?", new CreateNewPeriodical());
        requestMapping.put("GET:/adminPanel/periodicals/update/\\d+", new UpdatePeriodical());
        requestMapping.put("GET:/adminPanel/periodicals/discarded/delete/?", new DeleteDiscardedPeriodicals());
        requestMapping.put("POST:/adminPanel/users/\\d+/invoices/?", new PersistOneInvoice());

    }

    private HttpServletRequest request;

    public RequestProvider(HttpServletRequest request) {
        this.request = request;
    }

    public RequestProcessor getRequestProcessor() throws NoSuchElementException {
        String requestMethod = request.getMethod().toUpperCase();
        String requestURI = request.getRequestURI();
        System.out.println("getRequestProcessor(): requestURI = '" + requestURI + "'");

        Optional<Map.Entry<String, RequestProcessor>> mapping = requestMapping.entrySet()
                .stream()
                .filter(entry -> {
                    String methodPattern = entry.getKey().split(":")[0];
                    String[] methods = methodPattern.split("\\|");
//                    System.out.println("------------- methods from the pattern:");
//                    Arrays.asList(methods).forEach(System.out::println);

                    return Arrays.asList(methods).contains(requestMethod);
                })
                .filter(entry -> {
                    String urlPattern = entry.getKey().split(":")[1];
                    System.out.println("urlPattern = '" + urlPattern + "'");

                    return Pattern.matches(urlPattern, requestURI);
                })
                .findFirst();

        System.out.println("mapping = " + mapping);

        if (!mapping.isPresent()) {
            throw new NoSuchElementException(
                    String.format("There no mapping for such a request: '%s'.", requestURI));
        }

        return mapping.get().getValue();
    }
}
