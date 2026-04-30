package com.studyhive.spring_boot_docker;

import java.util.Objects;
import jakarta.persistence.*;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OauthProvider oauthProvider;

    @Size(max = 500)
    @Column(length = 500)
    private String bio;

    protected User() {
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public OauthProvider getOauthProvider() {
        return oauthProvider;
    }

    public void setOauthProvider(OauthProvider oauthProvider) {
        this.oauthProvider = oauthProvider;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof User user)) return false;
        return Objects.equals(id, user.id) && Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }
}