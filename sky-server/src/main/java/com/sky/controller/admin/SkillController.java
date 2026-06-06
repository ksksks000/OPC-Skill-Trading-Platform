package com.sky.controller.admin;

import com.sky.dto.SkillDTO;
import com.sky.dto.SkillPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SkillService;
import com.sky.vo.SkillVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 技能管理
 */
@RestController
@RequestMapping("/admin/skill")
@Slf4j
public class SkillController {

    @Autowired
    private SkillService skillService;

    /**
     * 新增技能
     */
    @PostMapping
    public Result save(@RequestBody SkillDTO skillDTO) {
        log.info("新增技能：{}", skillDTO);
        skillService.save(skillDTO);
        return Result.success();
    }

    /**
     * 分页查询技能
     */
    @GetMapping("/page")
    public Result<PageResult> page(SkillPageQueryDTO skillPageQueryDTO) {
        log.info("分页查询技能：{}", skillPageQueryDTO);
        PageResult pageResult = skillService.pageQuery(skillPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 修改技能
     */
    @PutMapping
    public Result update(@RequestBody SkillDTO skillDTO) {
        log.info("修改技能：{}", skillDTO);
        skillService.update(skillDTO);
        return Result.success();
    }

    /**
     * 启用/禁用技能
     */
    @PostMapping("/status/{status}")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        log.info("启用/禁用技能：{}，{}", status, id);
        skillService.startOrStop(status, id);
        return Result.success();
    }

    /**
     * 根据ID查询技能
     */
    @GetMapping("/{id}")
    public Result<SkillVO> getById(@PathVariable Long id) {
        log.info("查询技能详情：{}", id);
        SkillVO skillVO = skillService.getById(id);
        return Result.success(skillVO);
    }

    /**
     * 删除技能
     */
    @DeleteMapping
    public Result delete(Long id) {
        log.info("删除技能：{}", id);
        skillService.deleteById(id);
        return Result.success();
    }
}
