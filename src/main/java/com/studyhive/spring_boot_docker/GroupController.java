package com.studyhive.spring_boot_docker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/groups")
public class GroupController {
    @Autowired
    private StudyGroupRepository groupRepository;

    //--Entry input--
    @PostMapping
    public ResponseEntity<StudyGroup> createGroup(@RequestBody StudyGroup group, @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        group.setCreatorId(userId);

        StudyGroup savedGroups = groupRepository.save(group);
        return new ResponseEntity<>(savedGroups, HttpStatus.OK);
    }
    //--Implement Deletion
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        String currentUserID = jwt.getSubject();

        return groupRepository.findById(id)
                .map(group ->{
                    //--Will check if the logged-in user is actually the creator
                    if(group.getCreatorId().equals(currentUserID)){
                        return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
                    }
                    groupRepository.delete(group);
                    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
