package com.personalcrm.contact;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class BirthdayImportantDateSynchronizer {

    private static final String BIRTHDAY_TITLE = "Birthday";

    private final ImportantDateRepository importantDateRepository;

    public BirthdayImportantDateSynchronizer(ImportantDateRepository importantDateRepository) {
        this.importantDateRepository = importantDateRepository;
    }

    public void syncBirthdayDateFromContact(Contact contact) {
        List<ImportantDate> birthdayDates = findBirthdayDates(contact);

        if (contact.getBirthday() == null) {
            importantDateRepository.deleteAll(birthdayDates);
            return;
        }

        ImportantDate birthdayDate;
        if (birthdayDates.isEmpty()) {
            birthdayDate = importantDateRepository.save(new ImportantDate(
                    contact,
                    BIRTHDAY_TITLE,
                    contact.getBirthday(),
                    ImportantDateType.BIRTHDAY,
                    null
            ));
        } else {
            birthdayDate = birthdayDates.get(0);
            birthdayDate.updateDetails(
                    birthdayDate.getTitle(),
                    contact.getBirthday(),
                    ImportantDateType.BIRTHDAY,
                    birthdayDate.getDescription()
            );
        }

        deleteDuplicateBirthdayDates(birthdayDates, birthdayDate);
    }

    public void syncContactBirthdayFromImportantDate(Contact contact, ImportantDate importantDate) {
        if (importantDate.getType() == ImportantDateType.BIRTHDAY) {
            contact.updateBirthday(importantDate.getDate());
            deleteDuplicateBirthdayDates(findBirthdayDates(contact), importantDate);
            return;
        }

        syncContactBirthdayFromExistingDates(contact);
    }

    public void syncContactBirthdayAfterImportantDateDeletion(Contact contact) {
        syncContactBirthdayFromExistingDates(contact);
    }

    private void syncContactBirthdayFromExistingDates(Contact contact) {
        List<ImportantDate> birthdayDates = findBirthdayDates(contact);
        if (birthdayDates.isEmpty()) {
            contact.updateBirthday(null);
            return;
        }

        ImportantDate birthdayDate = birthdayDates.get(0);
        contact.updateBirthday(birthdayDate.getDate());
        deleteDuplicateBirthdayDates(birthdayDates, birthdayDate);
    }

    private List<ImportantDate> findBirthdayDates(Contact contact) {
        return importantDateRepository.findByContactIdAndTypeOrderByDateAscTitleAsc(
                contact.getId(),
                ImportantDateType.BIRTHDAY
        );
    }

    private void deleteDuplicateBirthdayDates(List<ImportantDate> birthdayDates, ImportantDate birthdayDateToKeep) {
        birthdayDates.stream()
                .filter(birthdayDate -> !birthdayDate.getId().equals(birthdayDateToKeep.getId()))
                .forEach(importantDateRepository::delete);
    }
}
