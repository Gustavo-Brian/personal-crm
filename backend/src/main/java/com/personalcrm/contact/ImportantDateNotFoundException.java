package com.personalcrm.contact;

public class ImportantDateNotFoundException extends RuntimeException {

    public ImportantDateNotFoundException(Long importantDateId) {
        super("Important date not found: " + importantDateId);
    }
}
