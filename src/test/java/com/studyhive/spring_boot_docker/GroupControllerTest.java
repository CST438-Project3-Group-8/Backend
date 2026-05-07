package com.studyhive.spring_boot_docker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GroupControllerTest {

    private GroupController groupController;
    private StudyGroupRepository groupRepository;
    private GroupMemberRepository groupMemberRepository;

    @BeforeEach
    void setUp() {
        groupRepository = mock(StudyGroupRepository.class);
        groupMemberRepository = mock(GroupMemberRepository.class);

        groupController = new GroupController(groupRepository, groupMemberRepository);
    }

    @Test
    void createGroupUsesJwtSubjectAsCreatorId() {
        StudyGroup group = new StudyGroup();
        group.setTitle("CS Study Group");

        when(groupRepository.save(any(StudyGroup.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<StudyGroup> response = groupController.createGroup(group, jwt("user-123"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCreatorId()).isEqualTo("user-123");
        verify(groupRepository).save(group);
    }

    @Test
    void deleteGroupReturnsForbiddenWhenRequesterIsNotCreator() {
        StudyGroup group = new StudyGroup();
        group.setCreatorId("owner-1");
        when(groupRepository.findById(5L)).thenReturn(Optional.of(group));

        ResponseEntity<Void> response = groupController.deleteGroup(5L, jwt("other-user"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void getAllGroupsReturnsRepositoryResults() {
        StudyGroup first = new StudyGroup();
        first.setTitle("Math");

        StudyGroup second = new StudyGroup();
        second.setTitle("History");

        when(groupRepository.findAll()).thenReturn(List.of(first, second));

        ResponseEntity<List<StudyGroup>> response = groupController.getAllGroups();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody())
                .extracting(StudyGroup::getTitle)
                .containsExactly("Math", "History");
    }

    @Test
    void joinGroupReturnsCreatedWhenUserIsNotAlreadyMember() {
        StudyGroup group = new StudyGroup();
        group.setMaxMembers(10);

        GroupMember savedMember = new GroupMember(3L, "user-123");

        when(groupRepository.findById(3L)).thenReturn(Optional.of(group));
        when(groupMemberRepository.existsByGroupIdAndUserId(3L, "user-123")).thenReturn(false);
        when(groupMemberRepository.countByGroupId(3L)).thenReturn(0L);
        when(groupMemberRepository.save(any(GroupMember.class))).thenReturn(savedMember);

        ResponseEntity<?> response = groupController.joinGroup(3L, jwt("user-123"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isInstanceOf(GroupMember.class);

        GroupMember body = (GroupMember) response.getBody();
        assertThat(body.getGroupId()).isEqualTo(3L);
        assertThat(body.getUserId()).isEqualTo("user-123");
    }

    @Test
    void joinGroupReturnsConflictWhenAlreadyMember() {
        StudyGroup group = new StudyGroup();

        when(groupRepository.findById(3L)).thenReturn(Optional.of(group));
        when(groupMemberRepository.existsByGroupIdAndUserId(3L, "user-123")).thenReturn(true);

        ResponseEntity<?> response = groupController.joinGroup(3L, jwt("user-123"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    private static Jwt jwt(String subject) {
        Instant issuedAt = Instant.now();
        return new Jwt(
                "token",
                issuedAt,
                issuedAt.plusSeconds(300),
                Map.of("alg", "none"),
                Map.of("sub", subject)
        );
    }
}