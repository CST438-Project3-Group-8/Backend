package com.studyhive.spring_boot_docker;

import java.util.Objects;
import jakarta.persistence.*;

@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue
    private int id;
    private String name;
    private String email;
    private String oauthProvider;
    private String bio;
    private String availability;

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getOauthProvider() {
        return oauthProvider;
    }

    public void setOauthProvider(String oauthProvider) {
        this.oauthProvider = oauthProvider;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof User user)) return false;
        return id == user.id && Objects.equals(name, user.name) && Objects.equals(email, user.email) && Objects.equals(oauthProvider, user.oauthProvider) && Objects.equals(bio, user.bio) && Objects.equals(availability, user.availability);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, oauthProvider, bio, availability);
    }
}
