package com.studyhive.spring_boot_docker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // Fixes AuthenticationPrincipal error
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {
    @Autowired
    private StudyGroupRepository groupRepository;

    //--Entry input--
    @PostMapping
    public ResponseEntity<StudyGroup> createGroup(@RequestBody StudyGroup group, @AuthenticationPrincipal Jwt jwt) {

        //String userId = jwt.getSubject();
        group.setCreatorId(jwt.getSubject());

        StudyGroup savedGroups = groupRepository.save(group);
        return new ResponseEntity<>(savedGroups, HttpStatus.CREATED);
    }
    //--Implement Deletion
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        String currentUserID = jwt.getSubject();

        return groupRepository.findById(id)
                .map(group -> {
                    // Ensure the IDs match before deleting
                    if (!group.getCreatorId().equals(currentUserID)) {
                        return new ResponseEntity<Void>(HttpStatus.FORBIDDEN);
                    }
                    groupRepository.delete(group);
                    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    //1. Get all study groups
    @GetMapping
    public ResponseEntity<List<StudyGroup>>getAllGroups(){
        List<StudyGroup> groups = groupRepository.findAll();
        return new ResponseEntity<>(groups, HttpStatus.OK);
    }
    //2. Get a single group by ID
    @GetMapping("/{id}")
    public ResponseEntity<StudyGroup> getGroupById(@PathVariable Long id){
        return groupRepository.findById(id)
                .map(group -> new ResponseEntity<>(group, HttpStatus.OK))
                .orElse(ResponseEntity.notFound().build());
    }
}
