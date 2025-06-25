package com.flash.sanitization.db.exception;

/**
 * Thrown when a database operation cannot be performed because a record already exists.
 * Primarily used in cases of inserts.
 */
public class RecordExistsException extends Exception {

    public RecordExistsException(String operation, String record) {
        super("%s record already exists cannot %s".formatted(operation, record));
    }
}
