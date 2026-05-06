package com.studyhive.spring_boot_docker;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    boolean existsByGroupIdAndUserId(Long groupId, String userId);

    Optional<GroupMember> findByGroupIdAndUserId(Long groupId, String userId);

    List<GroupMember> findByGroupId(Long groupId);

    List<GroupMember> findByUserId(String userId);

    long countByGroupId(Long groupId);
}