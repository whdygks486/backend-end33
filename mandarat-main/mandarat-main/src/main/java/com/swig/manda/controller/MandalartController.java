package com.swig.manda.controller;


import com.swig.manda.config.auth.PrincipalDetails;
import com.swig.manda.dto.DetailDto;
import com.swig.manda.dto.MainTopicDto;
import com.swig.manda.service.MadalartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;



@CrossOrigin(origins ="*", allowedHeaders = "*")
@RestController
@RequestMapping("/members/{userId}")
public class MandalartController {

    private final MadalartService madalartService;

    @Autowired
    public MandalartController(MadalartService madalartService) {
        this.madalartService = madalartService;
    }

    @GetMapping("/main")
    public ResponseEntity<Object> getAllMainTopics(@PathVariable String userId) {
        List<MainTopicDto> mainTopics = madalartService.getAllMainTopicsByUserId(userId);
        if (mainTopics == null) {
            mainTopics = new ArrayList<>();
        }
        return ResponseEntity.ok(mainTopics);
    }

    @GetMapping("/main/{topicId}")
    public ResponseEntity<MainTopicDto> getMainTopicWithDetails(@PathVariable String userId, @PathVariable Long topicId) {
        MainTopicDto mainTopic = madalartService.getMainTopicWithSubTopicsByUserId(topicId, userId);
        return ResponseEntity.ok(mainTopic);
    }

    @PostMapping("/main")
    public ResponseEntity<MainTopicDto> saveMainTopic(@PathVariable String userId, @Valid @RequestBody MainTopicDto mainTopicDto) {
        mainTopicDto.setUserId(userId);
        MainTopicDto savedTopic = madalartService.saveMainTopics(mainTopicDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTopic);
    }

    @PostMapping("/details")
    public ResponseEntity<DetailDto> saveDetail(@PathVariable String userId, @Valid @RequestBody DetailDto detailDto) {
        detailDto.setUserId(userId);
        DetailDto savedDetail = madalartService.saveDetail(detailDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDetail);
    }

    @PutMapping("/main/{topicId}")
    public ResponseEntity<MainTopicDto> updateMainTopic(@PathVariable String userId, @Valid @PathVariable Long topicId, @RequestBody MainTopicDto mainTopicDto) {
        MainTopicDto updatedTopic = madalartService.updateMainTopic(topicId, mainTopicDto);
        if (updatedTopic == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedTopic);
    }

    @PutMapping("/details/{detailId}")
    public ResponseEntity<DetailDto> updateDetail(@PathVariable String userId, @Valid @PathVariable Long detailId, @RequestBody DetailDto detailDto) {
        DetailDto updatedDetail = madalartService.updateDetail(detailId, detailDto);
        if (updatedDetail == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedDetail);
    }
}

