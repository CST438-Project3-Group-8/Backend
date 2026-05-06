package com.studyhive.spring_boot_docker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.util.ReflectionTestUtils;

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

    @BeforeEach
    void setUp() {
        groupController = new GroupController();
        groupRepository = mock(StudyGroupRepository.class);
        ReflectionTestUtils.setField(groupController, "groupRepository", groupRepository);
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
        assertThat(response.getBody()).extracting(StudyGroup::getTitle).containsExactly("Math", "History");
    }

    private static Jwt jwt(String subject) {
        Instant issuedAt = Instant.now();
        return new Jwt("token", issuedAt, issuedAt.plusSeconds(300), Map.of("alg", "none"), Map.of("sub", subject));
    }
}
