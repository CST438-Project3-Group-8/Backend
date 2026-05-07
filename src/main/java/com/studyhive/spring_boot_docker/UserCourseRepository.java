package com.studyhive.spring_boot_docker;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserCourseRepository extends JpaRepository<UserCourse, Long> {
    List<UserCourse> findByUserId(String userId);
    Optional<UserCourse> findByUserIdAndCourse_Id(String userId, Long courseId);
    boolean existsByUserIdAndCourse_Id(String userId, Long courseId);
    void deleteByUserIdAndCourse_Id(String userId, Long courseId);
    void deleteAllByUserId(String userId);
}
