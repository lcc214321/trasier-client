package com.trasier.client.utils;

public class Precondition {

    public static void notNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException("Argument " + fieldName + " cannot be null.");
        }
    }
}
