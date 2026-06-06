package com.sky.controller.user;

import com.sky.dto.SkillPageQueryDTO;
import com.sky.entity.Skill;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SkillService;
import com.sky.vo.SkillVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户端技能浏览控制器
 *
 * 提供用户端（买家端）的技能查询接口，无需管理员权限。
 * 与管理端 SkillController 的区别：
 * - 管理端 /admin/skill：需要管理员 token，支持增删改查
 * - 用户端 /user/skill：需要用户 token，仅支持查询
 *
 * 解决问题：前端首页和详情页原本调用 /admin/skill/* 接口，
 * 但用户 token 无法通过管理端 JWT 校验，导致 401 错误。
 */
@RestController
@RequestMapping("/user/skill")
@Slf4j
public class UserSkillController {

    @Autowired
    private SkillService skillService;

    /**
     * 分页查询上架中的技能
     * 仅返回 status=1（上架）的技能
     */
    @GetMapping("/page")
    public Result<PageResult> page(SkillPageQueryDTO skillPageQueryDTO) {
        log.info("用户端分页查询技能：{}", skillPageQueryDTO);
        // 默认只查询上架中的技能
        skillPageQueryDTO.setStatus(1);
        PageResult pageResult = skillService.pageQuery(skillPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 根据ID查询技能详情
     * 仅返回上架中的技能
     */
    @GetMapping("/{id}")
    public Result<SkillVO> getById(@PathVariable Long id) {
        log.info("用户端查询技能详情：{}", id);
        SkillVO skillVO = skillService.getById(id);
        // 如果技能已下架，返回空
        if (skillVO != null && skillVO.getStatus() != null && skillVO.getStatus() != 1) {
            return Result.error("该技能已下架");
        }
        return Result.success(skillVO);
    }

    /**
     * 根据分类ID查询技能列表
     */
    @GetMapping("/list")
    public Result<List<Skill>> listByCategory(@RequestParam(required = false) Long categoryId) {
        log.info("用户端按分类查询技能：categoryId={}", categoryId);
        List<Skill> skills = skillService.searchSkills(
                categoryId != null ? String.valueOf(categoryId) : null,
                null, null
        );
        return Result.success(skills);
    }

    /**
     * 根据关键词搜索技能
     * 同时匹配技能名称和标签
     */
    @GetMapping("/search")
    public Result<List<Skill>> search(@RequestParam String keyword) {
        log.info("用户端搜索技能：keyword={}", keyword);
        List<Skill> skills = skillService.searchSkills(keyword, null, null);
        return Result.success(skills);
    }
}
