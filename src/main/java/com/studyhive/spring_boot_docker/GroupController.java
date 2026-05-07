package com.studyhive.spring_boot_docker;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final StudyGroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;

    public GroupController(
            StudyGroupRepository groupRepository,
            GroupMemberRepository groupMemberRepository
    ) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
    }

    // Create group
    @PostMapping
    public ResponseEntity<StudyGroup> createGroup(
            @RequestBody StudyGroup group,
            @AuthenticationPrincipal Jwt jwt
    ) {
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        group.setCreatorId(jwt.getSubject());

        StudyGroup savedGroup = groupRepository.save(group);
        return new ResponseEntity<>(savedGroup, HttpStatus.CREATED);
    }

    // Get all study groups
    @GetMapping
    public ResponseEntity<List<StudyGroup>> getAllGroups() {
        List<StudyGroup> groups = groupRepository.findAll();
        return new ResponseEntity<>(groups, HttpStatus.OK);
    }

    // Get a single group by ID
    @GetMapping("/{id}")
    public ResponseEntity<StudyGroup> getGroupById(@PathVariable Long id) {
        return groupRepository.findById(id)
                .map(group -> new ResponseEntity<>(group, HttpStatus.OK))
                .orElse(ResponseEntity.notFound().build());
    }

    // Join group
    @PostMapping("/{groupId}/join")
    public ResponseEntity<?> joinGroup(
            @PathVariable Long groupId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
        }

        String userId = jwt.getSubject();

        return groupRepository.findById(groupId)
                .map(group -> {
                    if (groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body("User is already a member of this group");
                    }

                    long currentMembers = groupMemberRepository.countByGroupId(groupId);
                    if (group.getMaxMembers() != null && currentMembers >= group.getMaxMembers()) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body("Group is already full");
                    }

                    GroupMember member = new GroupMember(groupId, userId);
                    GroupMember savedMember = groupMemberRepository.save(member);

                    return ResponseEntity.status(HttpStatus.CREATED).body(savedMember);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Leave group
    @DeleteMapping("/{groupId}/leave")
    public ResponseEntity<Void> leaveGroup(
            @PathVariable Long groupId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userId = jwt.getSubject();

        return groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .map(member -> {
                    groupMemberRepository.delete(member);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Get members of a group
    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<GroupMember>> getGroupMembers(@PathVariable Long groupId) {
        if (!groupRepository.existsById(groupId)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(groupMemberRepository.findByGroupId(groupId));
    }
    // Check whether the current user has joined a group
    @GetMapping("/{groupId}/membership")
    public ResponseEntity<?> getMembership(
            @PathVariable Long groupId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = jwt != null ? jwt.getSubject() : "test-user-id";

        boolean joined = groupMemberRepository.existsByGroupIdAndUserId(groupId, userId);

        return ResponseEntity.ok(Map.of(
                "groupId", groupId,
                "joined", joined
        ));
    }

    // Delete group
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String currentUserId = jwt.getSubject();

        return groupRepository.findById(id)
                .map(group -> {
                    if (!group.getCreatorId().equals(currentUserId)) {
                        return new ResponseEntity<Void>(HttpStatus.FORBIDDEN);
                    }

                    groupRepository.delete(group);
                    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    //added endpoint
    @GetMapping("/me/joined")
    public ResponseEntity<List<StudyGroup>> getMyJoinedGroups(
            @AuthenticationPrincipal Jwt jwt
    ) {
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userId = jwt.getSubject();

        List<Long> groupIds = groupMemberRepository.findByUserId(userId)
                .stream()
                .map(GroupMember::getGroupId)
                .toList();

        List<StudyGroup> groups = groupRepository.findAllById(groupIds);

        return ResponseEntity.ok(groups);
    }

}