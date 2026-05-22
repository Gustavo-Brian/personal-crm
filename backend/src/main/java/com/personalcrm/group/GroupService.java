package com.personalcrm.group;

import com.personalcrm.user.User;
import com.personalcrm.user.UserRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GroupService {

    private final ContactGroupRepository groupRepository;
    private final UserRepository userRepository;

    public GroupService(ContactGroupRepository groupRepository, UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> listGroups(String authenticatedEmail) {
        User owner = findAuthenticatedUser(authenticatedEmail);

        return groupRepository.findByOwnerIdOrderByNameAsc(owner.getId())
                .stream()
                .map(GroupResponse::from)
                .toList();
    }

    @Transactional
    public GroupResponse createGroup(String authenticatedEmail, GroupRequest request) {
        User owner = findAuthenticatedUser(authenticatedEmail);
        ContactGroup group = new ContactGroup(
                owner,
                normalizeRequired(request.name()),
                normalizeOptional(request.description()),
                normalizeColorHex(request.colorHex())
        );

        return GroupResponse.from(groupRepository.save(group));
    }

    @Transactional(readOnly = true)
    public GroupResponse getGroup(String authenticatedEmail, Long groupId) {
        User owner = findAuthenticatedUser(authenticatedEmail);
        ContactGroup group = findOwnedGroup(groupId, owner.getId());

        return GroupResponse.from(group);
    }

    @Transactional
    public GroupResponse updateGroup(String authenticatedEmail, Long groupId, GroupRequest request) {
        User owner = findAuthenticatedUser(authenticatedEmail);
        ContactGroup group = findOwnedGroup(groupId, owner.getId());
        group.updateDetails(
                normalizeRequired(request.name()),
                normalizeOptional(request.description()),
                normalizeColorHex(request.colorHex())
        );

        return GroupResponse.from(group);
    }

    @Transactional
    public void deleteGroup(String authenticatedEmail, Long groupId) {
        User owner = findAuthenticatedUser(authenticatedEmail);
        ContactGroup group = findOwnedGroup(groupId, owner.getId());

        groupRepository.delete(group);
    }

    private User findAuthenticatedUser(String authenticatedEmail) {
        return userRepository.findByEmail(normalizeEmail(authenticatedEmail))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private ContactGroup findOwnedGroup(Long groupId, Long ownerId) {
        return groupRepository.findByIdAndOwnerId(groupId, ownerId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeRequired(String value) {
        return value.trim();
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String normalizeColorHex(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
