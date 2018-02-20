package com.trasier.client.utils;

/**
 * Created by lukasz on 04.02.18.
 */
public class Precondition {

    public static void notNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException("Argument " + fieldName + " cannot be null.");
        }
    }
}
