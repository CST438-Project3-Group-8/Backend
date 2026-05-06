package com.studyhive.spring_boot_docker;

import jakarta.persistence.*;

@Entity
@Table(name = "user_courses",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "course_id"}))
public class UserCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    public UserCourse() {}

    public UserCourse(String userId, Course course) {
        this.userId = userId;
        this.course = course;
    }

    public Long getId() { return id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
}
