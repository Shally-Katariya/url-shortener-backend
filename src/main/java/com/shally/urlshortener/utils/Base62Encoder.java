package com.shally.urlshortener.utils;

public class Base62Encoder {

    private static final String CHARSET =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public static String encode(long num) {

        StringBuilder sb = new StringBuilder();

        while (num > 0) {
            int remainder = (int) (num % 62);
            sb.append(CHARSET.charAt(remainder));
            num = num / 62;
        }

        return sb.reverse().toString();
    }
}