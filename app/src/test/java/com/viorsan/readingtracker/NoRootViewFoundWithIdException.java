package com.viorsan.readingtracker;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 10.01.15.
 * Thrown by TestHelpers's screenshot if no root view found with specified id
 */
public class NoRootViewFoundWithIdException extends Exception {

    /**
     * Constructs a {@code NoRootViewFoundWithIdException} with no specified detail
     * message.
     */
    public NoRootViewFoundWithIdException() {}

    /**
     * Constructs a {@code NoRootViewFoundWithIdException} with the specified detail
     * message.
     *
     * @param message the detail message
     */
    public NoRootViewFoundWithIdException(String message) {
        super(message);
    }
}
