package com.personalcrm.group;

import com.personalcrm.contact.ContactResponse;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping
    public List<GroupResponse> listGroups(Principal principal) {
        return groupService.listGroups(principal.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupResponse createGroup(Principal principal, @Valid @RequestBody GroupRequest request) {
        return groupService.createGroup(principal.getName(), request);
    }

    @GetMapping("/{id}")
    public GroupResponse getGroup(Principal principal, @PathVariable Long id) {
        return groupService.getGroup(principal.getName(), id);
    }

    @PutMapping("/{id}")
    public GroupResponse updateGroup(
            Principal principal,
            @PathVariable Long id,
            @Valid @RequestBody GroupRequest request
    ) {
        return groupService.updateGroup(principal.getName(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGroup(Principal principal, @PathVariable Long id) {
        groupService.deleteGroup(principal.getName(), id);
    }

    @GetMapping("/{groupId}/contacts")
    public List<ContactResponse> listGroupContacts(Principal principal, @PathVariable Long groupId) {
        return groupService.listGroupContacts(principal.getName(), groupId);
    }

    @PostMapping("/{groupId}/contacts/{contactId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ContactResponse addContactToGroup(
            Principal principal,
            @PathVariable Long groupId,
            @PathVariable Long contactId
    ) {
        return groupService.addContactToGroup(principal.getName(), groupId, contactId);
    }

    @DeleteMapping("/{groupId}/contacts/{contactId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeContactFromGroup(
            Principal principal,
            @PathVariable Long groupId,
            @PathVariable Long contactId
    ) {
        groupService.removeContactFromGroup(principal.getName(), groupId, contactId);
    }
}
