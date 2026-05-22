package com.personalcrm.contact;

public class AcademicFormationNotFoundException extends RuntimeException {

    public AcademicFormationNotFoundException(Long formationId) {
        super("Academic formation not found: " + formationId);
    }
}
