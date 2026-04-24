package com.studyhive.spring_boot_docker;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.FluentQuery;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {
    //--should be empty. Spring should provide safe, findId and deleteid auto


}