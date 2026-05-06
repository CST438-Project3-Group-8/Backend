package com.studyhive.spring_boot_docker;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByUserId(String userId);
    Optional<User> findByEmail(String email);
}
