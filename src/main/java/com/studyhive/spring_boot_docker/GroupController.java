package com.studyhive.spring_boot_docker;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/groups")
@CrossOrigin(origins = "http://localhost:5173")
public class GroupController {

    private final StudyGroupRepository studyGroupRepository;

    public GroupController(StudyGroupRepository studyGroupRepository) {
        this.studyGroupRepository = studyGroupRepository;
    }

    @GetMapping
    public java.util.List<StudyGroup> getAllGroups() {
        return studyGroupRepository.findAll();
    }

    @PostMapping
    public StudyGroup createGroup(@RequestBody CreateGroupRequest request) {
        StudyGroup group = new StudyGroup();
        group.setGroupName(request.getGroupName());
        group.setCourseCode(request.getCourseCode());
        group.setMaxMembers(request.getMaxMembers());
        group.setDescription(request.getDescription());
        group.setMeetingMode(request.getMeetingMode());
        group.setLocation(request.getLocation());
        group.setSchedule(request.getSchedule());

        return studyGroupRepository.save(group);
    }
}