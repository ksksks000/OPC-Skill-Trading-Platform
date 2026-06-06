package com.sky.service;

import com.sky.dto.SkillDTO;
import com.sky.dto.SkillPageQueryDTO;
import com.sky.entity.Skill;
import com.sky.result.PageResult;
import com.sky.vo.SkillVO;

import java.util.List;

public interface SkillService {

    /**
     * 新增技能
     */
    void save(SkillDTO skillDTO);

    /**
     * 分页查询技能
     */
    PageResult pageQuery(SkillPageQueryDTO skillPageQueryDTO);

    /**
     * 修改技能
     */
    void update(SkillDTO skillDTO);

    /**
     * 启用/禁用技能
     */
    void startOrStop(Integer status, Long id);

    /**
     * 根据ID查询技能
     */
    SkillVO getById(Long id);

    /**
     * 根据条件搜索技能（供AI调用）
     */
    List<Skill> searchSkills(String category, String tag, Double minPrice);

    /**
     * 删除技能
     */
    void deleteById(Long id);
}
