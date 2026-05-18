package com.personalcrm.contact;

import static org.assertj.core.api.Assertions.assertThat;

import com.personalcrm.user.User;
import com.personalcrm.user.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ContactRepositoryTest {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void persistsContactsOwnedByUser() {
        User owner = userRepository.saveAndFlush(new User("Owner User", "owner@example.com", "encoded-password"));
        Contact contact = new Contact(
                owner,
                "Grace Hopper",
                "US Navy",
                "Computer Scientist",
                LocalDate.of(1906, 12, 9)
        );

        contactRepository.saveAndFlush(contact);

        Optional<Contact> found = contactRepository.findByIdAndOwnerId(contact.getId(), owner.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Grace Hopper");
        assertThat(found.get().getOwner().getId()).isEqualTo(owner.getId());
        assertThat(found.get().getBirthday()).isEqualTo(LocalDate.of(1906, 12, 9));
        assertThat(found.get().getCreatedAt()).isNotNull();
        assertThat(found.get().getUpdatedAt()).isNotNull();
    }

    @Test
    void listsAndCountsContactsByOwner() {
        User firstOwner = userRepository.save(new User("First Owner", "first-owner@example.com", "encoded-password"));
        User secondOwner = userRepository.save(new User("Second Owner", "second-owner@example.com", "encoded-password"));
        userRepository.flush();

        Contact alan = new Contact(firstOwner, "Alan Turing", "Bletchley Park", "Mathematician", null);
        Contact ada = new Contact(firstOwner, "Ada Lovelace", null, "Mathematician", null);
        Contact katherine = new Contact(secondOwner, "Katherine Johnson", "NASA", "Mathematician", null);
        contactRepository.saveAllAndFlush(List.of(alan, ada, katherine));

        List<Contact> firstOwnerContacts = contactRepository.findByOwnerIdOrderByNameAsc(firstOwner.getId());

        assertThat(firstOwnerContacts)
                .extracting(Contact::getName)
                .containsExactly("Ada Lovelace", "Alan Turing");
        assertThat(contactRepository.countByOwnerId(firstOwner.getId())).isEqualTo(2);
        assertThat(contactRepository.countByOwnerId(secondOwner.getId())).isEqualTo(1);
        assertThat(contactRepository.existsByIdAndOwnerId(katherine.getId(), firstOwner.getId())).isFalse();
        assertThat(contactRepository.findByIdAndOwnerId(katherine.getId(), firstOwner.getId())).isEmpty();
    }
}
