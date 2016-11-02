package com.stolser.javatraining.block02.morelessgame.view;

import java.util.Locale;

/**
 * Implements The Adapter Design Pattern.
 */
public interface ViewPrinter {
    Locale DEFAULT_LOCALE = new Locale("en", "US");

    void printlnString(String string);
    void printString(String string);
    void printlnMessageWithKey(String bundleName, String key);
    void printMessageWithKey(String bundleName, String key);
    void setLocale(Locale locale);
    String getMessageWithKey(String bundleName, String key);
    String getLocalizedNumber(Number number);
}
