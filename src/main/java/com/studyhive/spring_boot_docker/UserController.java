package com.studyhive.spring_boot_docker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<User> createOrUpdateUser(@RequestBody User user, @AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaimAsString("email");

        if (email == null || email.isBlank()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        User savedUser = userRepository.findByEmail(email)
                .map(existingUser -> {
                    existingUser.setName(user.getName());
                    existingUser.setEmail(email);
                    existingUser.setOauthProvider(user.getOauthProvider());
                    existingUser.setBio(user.getBio());
                    existingUser.setAvailability(user.getAvailability());
                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> {
                    user.setEmail(email);
                    return userRepository.save(user);
                });

        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaimAsString("email");

        if (email == null || email.isBlank()) {
            return ResponseEntity.notFound().build();
        }

        return userRepository.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
