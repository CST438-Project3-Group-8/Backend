package com.studyhive.spring_boot_docker;


import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping
    public @Valid User createUser(@Valid @RequestBody User user, @AuthenticationPrincipal Jwt jwt) {
        user.setUserId(jwt.getId());
        user.setEmail(jwt.getClaimAsString("email"));
        return userRepository.save(user);
    }

    @GetMapping
    public Map<String, Object> getUser(@AuthenticationPrincipal Jwt jwt) {
        return Map.of(
                "userId", jwt.getSubject(),
                "email", jwt.getClaimAsString("email"),
                "role", jwt.getClaimAsString("role")
        );
    }

    @GetMapping("/me")
    public ResponseEntity<User> getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        List<User> found = userRepository.findByUserId(jwt.getSubject());
        if (found.isEmpty()) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(found.get(0));
    }


    @PutMapping
    public ResponseEntity<User> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        if (jwt == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        List<User> found = userRepository.findByUserId(jwt.getSubject());
        if (found.isEmpty()) return ResponseEntity.notFound().build();

        User user = found.get(0);
        user.setName(request.name());
        user.setBio(request.bio());

        return ResponseEntity.ok(userRepository.save(user));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
