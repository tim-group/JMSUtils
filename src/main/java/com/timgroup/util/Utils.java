package com.timgroup.util;

import java.net.URI;

public class Utils {
    
    public static <T> T defaulting(T value, T nullValue, T defaultValue) {
        if (equals(value, nullValue)) return defaultValue;
        else return value;
    }
    
    public static boolean equals(Object a, Object b) {
        return (a == b) || (a != null && b != null && a.equals(b));
    }
    
    public static String stripPrefix(String string, String prefix) {
        if (string.startsWith(prefix)) {
            string = string.substring(prefix.length());
        }
        return string;
    }
    
    public static String username(URI uri) {
        String userInfo = uri.getUserInfo();
        if (userInfo == null) return null;
        int colon = userInfo.indexOf(':');
        return (colon != -1) ? userInfo.substring(0, colon) : userInfo;
    }
    
    public static String password(URI uri) {
        String userInfo = uri.getUserInfo();
        if (userInfo == null) return null;
        int colon = userInfo.indexOf(':');
        return (colon != -1) ? userInfo.substring(colon + 1) : null;
    }
    
    private Utils() {}
    
}
