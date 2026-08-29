package com.club.controller;

import com.club.common.Result;
import com.club.entity.MemberLevel;
import com.club.service.MemberLevelService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/member-levels")
public class MemberLevelController {

    private final MemberLevelService levelService;

    public MemberLevelController(MemberLevelService levelService) {
        this.levelService = levelService;
    }

    @GetMapping
    public Result<List<MemberLevel>> list() {
        return Result.success(levelService.listEnabled());
    }
}
