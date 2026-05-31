package com.hci.scholarship.service;

import java.util.Locale;
import java.util.ResourceBundle;

public class LocalizationService {
    private Locale locale = new Locale("sq");
    private ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", locale);

    public ResourceBundle bundle() {
        return bundle;
    }

    public String get(String key) {
        return bundle.containsKey(key) ? bundle.getString(key) : key;
    }

    public void setLanguage(String languageCode) {
        locale = new Locale(languageCode);
        bundle = ResourceBundle.getBundle("i18n.messages", locale);
    }

    public String currentLanguage() {
        return locale.getLanguage();
    }
}

