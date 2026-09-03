package com.tenpearls.contactmanagementsystem.constant;

public class Constant {

    private Constant() {
        throw new IllegalStateException("Utility class");
    }
    public static final String PHOTO_DIRECTORY = System.getProperty("user.home") + "/Downloads/uploads/";
    public static final String X_REQUESTED_WITH = "X-REQUESTED-WITH";
}