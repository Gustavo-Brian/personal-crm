package com.personalcrm.group;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactGroupMembershipRepository extends JpaRepository<ContactGroupMembership, Long> {

    List<ContactGroupMembership> findByGroupIdAndGroupOwnerIdAndContactOwnerIdOrderByContactNameAsc(
            Long groupId,
            Long groupOwnerId,
            Long contactOwnerId
    );

    Optional<ContactGroupMembership> findByGroupIdAndContactId(Long groupId, Long contactId);

    boolean existsByGroupIdAndContactId(Long groupId, Long contactId);
}
