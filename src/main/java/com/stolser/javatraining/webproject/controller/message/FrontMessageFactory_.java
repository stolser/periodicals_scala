package com.stolser.javatraining.webproject.controller.message;

import com.stolser.javatraining.webproject.controller.message.FrontendMessage.MessageType$;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains methods for generating different types of frontend messages: 'success', 'info', 'warning',
 * 'error'. Implements the Flyweight Design Pattern.
 */
public class FrontMessageFactory_ {
    private static Map<String, FrontendMessage> messagesSuccess = new HashMap<>();
    private static Map<String, FrontendMessage> messagesInfo = new HashMap<>();
    private static Map<String, FrontendMessage> messagesWarning = new HashMap<>();
    private static Map<String, FrontendMessage> messagesError = new HashMap<>();

    private FrontMessageFactory_() {
    }

    private static class InstanceHolder {
        private static final FrontMessageFactory_ INSTANCE = new FrontMessageFactory_();
    }

    public static FrontMessageFactory_ getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public FrontendMessage getSuccess(String messageKey) {
        return getMessageFromCache(messagesSuccess, MessageType$.MODULE$.SUCCESS(), messageKey);
    }

    public FrontendMessage getInfo(String messageKey) {
        return getMessageFromCache(messagesInfo, MessageType$.MODULE$.INFO(), messageKey);
    }

    public FrontendMessage getWarning(String messageKey) {
        return getMessageFromCache(messagesWarning, MessageType$.MODULE$.WARNING(), messageKey);
    }

    public FrontendMessage getError(String messageKey) {
        return getMessageFromCache(messagesError, MessageType$.MODULE$.ERROR(), messageKey);
    }

    private FrontendMessage getMessageFromCache(Map<String, FrontendMessage> cache,
                                                FrontendMessage.MessageType$.Value messageType, String messageKey) {
        if (!cache.containsKey(messageKey)) {
            cache.put(messageKey, FrontendMessage.apply(messageKey, messageType));
        }

        return cache.get(messageKey);
    }
}
